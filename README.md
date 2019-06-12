#NBSQL 

[TOC]

## 1. 项目介绍

本项目为关系型数据库管理系统的构建。系统采用客户端/服务器架构。 项目共分为四个核心模块：存储模块、元数据管理模块、查询模块、客户端通信模块。

## 2. 功能介绍

本项目已经实现了**存储模块、元数据管理模块、查询模块、客户端通信模块的设计（实现基本功能及所有加分项）**。具体功能如下：

- 存储模块
  - 支持记录的持久化存储：持久化存储数据表schema、索引树(**支持多索引树，多键值索引**)、原始数据。
  - 支持对记录的增删改查：支持插入、更新、删除数据。**支持多种查询条件下的数据检索（查询条件间可进行and,or操作）**。
  - 支持五种数据类型：支持int,long,float,Double,String数据类型。
  - **支持高效的文件格式**：使用分页结构进行持久化，并实现碎片整理与溢出处理。

- 元数据管理模块
  - 创建、删除表。
  - 将表和默认数据库的元数据进行持久化。
  - 重启数据库时从持久化元数据中恢复系统信息。
  - **创建、删除数据库实例及切换数据库。**

- 查询模块

  - 解析SQL语句，包括基础语句及所有加分语句（详见附录）。

  - **使用Hash Join以及cache机制实现查询优化。**
  - **支持多列主键与新建索引。**
  - **where/on支持完整的and/or组合。**
  - **支持select 选择表达式。**
  - **支持三张表以上的join/ outer join/ natural join/ left outer join/ right outer join。**

- 客户端通信模块

  - **实现客户端GUI。**
  - 命令行发送SQL，Import SQL语句文件，并计算用时。

- 多用户连接

  - 实现多客户端连接，实时同步数据库。
  - **实现共享资源加锁。**

## 3. 系统设计

程序类图如下：

![class](.\pic\class.png)

| 类名          | 简介                                      |
| ------------- | ----------------------------------------- |
| BPlusTreeNode | 实现索引树节点类型                        |
| BPlusTree     | 实现索引树数据结构以及增删改查节点操作    |
| FileManager   | 实现文件管理类，实现disk数据的增删改查    |
| Table         | 实现数据表，完成单变的增删改查操作        |
| Database      | 实现数据库，完成数据库以及多表操作        |
| CacheTimer    | 实现定期更新Cache                         |
| Server        | 实现服务端，接受命令，使用parser解析      |
| Client        | 实现客户端，传输命令，显示结果            |
| Parser        | 实现parser，解析SQL语句，并且执行相应操作 |

执行顺序图如下：

![建筑结构图2 (1)](.\pic\seq.png) 

- 客户端通过网络模块向服务端传输SQL命令
- paser解析SQL，并调用database中的接口
- database操作table访问、更新或者删除cache或者disk上的数据

### 3.1 存储模块
#### 3.1.1 文件存储

此部分实现了FileManager，定义文件的分页结构，所有持久化操作均由此类实现。

#####3.1.1.1 分页结构

对于一个数据表，其schema、索引结构、数据均存储在一个文件中。对文件进行分页操作，划分前四页为shcema存储区，之后为索引及数据存储区。

对于分页，前八个byte存储页面的header。前四个byte存储当前页面可以写入的位置，后四个byte存储页面的数据内容。每次写入操作均会逐次寻找页面直到找到合适大小的空闲区域。

![page](.\pic\page.png)

#####3.1.1.2 碎片整理及溢出处理

在交替的插入删除过程中，可能存在文件碎片。我们实现了简易碎片回收机制：如果删除的记录位于页面最后，则回收碎片。如果页面记录全被删除，则回收页面。

大数据可能记录长度超过一个页面。为此，我们实现了溢出处理方式。对于溢出的分页，它的header为八个byte。前四个byte记录-1，用来区分是否溢出，后四个byte记录占用页面的长度。在遍历寻找空闲空间过程中会跳过大数据记录所占用的页面来寻找。

####3.1.2 B+树

此部分实现了磁盘上的B+树，以primary key或index作为key值（支持多列），作为数据库的核心数据结构。
##### 3.1.2.1 B+树的节点

节点由三个类实现:
* BPlusTreeNode：基类，抽象类
* BPlusTreeLeafNode：叶节点，继承BPlusTreeNode
* BPlusTreeInnerNode：内部节点，继承BPlusTreeNode

为便于数据的可持久化，基类中存放了所有的数据，两个子类仅在方法上有所区别。**并且对于同一棵树来说，最终存放在文件中的节点的长度是固定的。**

![node](.\pic\node.png)

节点的核心数据为key列表、pointer列表，叶节点和内部节点在pointer数组的含义上有一些细微的不同。叶节点的pointer存储的是data在文件中的offset，而内部节点的pointer存储的是子节点在data文件中的offset。叶节点的pointer长度等于key的长度，而内部节点的pointer长度则等于key的长度+1。

#####3.1.2.2 B+树的原理

基本实现无异，此处**B+树的实现重点主要在于需要将树的节点信息以及数据信息存放在磁盘上**而非内存当中，因此在对节点的更新、增加、删除的过程中，需要在合适的时机更新在文件中的信息，不能过于频繁导致效率低下也不能遗漏某处的更新同步。

### 3.2 元数据管理模块

此部分实现了关系型数据库的基础结构：Database 和 Table。

- **一个数据库具有多个数据表**，数据库实例Database使用一维数组记录当前数据库中的数据表（Table）。切换数据库时，更新数据表数组。
- **一个数据表具有多棵索引树**，使用一维数组记录所有索引树。
- **一个索引树具有多列键值**，使用二维数组记录多棵索引树的多个键值。

### 3.3 查询模块

#### 3.3.1 表查询

##### 3.3.1.1 单表查询

实现了完整的and or查询方式，使用ORM中常见的条件格式：
```C
[[["col1", 0, 2, true], ["col2", 1, "sad", true]], [["col3", 3, "col4", false]]]
```
内层数组之间的关系为and，外层数组之间的关系为or。false表示该条件的左右两边均为列名。
* 由于支持多列作为索引及多索引，对于and连接的多个条件，首先会检查是否与table中的某棵索引树中的key完全匹配，匹配之后直接以此多列作为key值进行查询。（前提是关系均为等于）
* 对于其他情况，首先会判断单一条件中的列是否为某个多列索引中的第一列，若是则以该列作为key进行查询。（关系不能为不等于）
* 对于剩余情况，将扫描所有数据并进行查询，并返回结果。
  对于查询结果按照逻辑关系进行取交取并运算。

##### 3.3.1.2 多表查询

这里主要介绍多表join操作如何实现。这里我们使用的是hash join的方式。

- 两表join：对于语句`A join B on A.a = B.b`，首先需要将`on`条件中的`A.a = B.b`这个condition提取出来。之后取出表A的a列，将属性值映射为hash code作为key，将一行数据的offset作为value，建立hash Table，同样可以提取出B的hash Table。之后遍历B的hash table即可获得到所有join数据。join之后得到结果如下：

  ```sql
  // HashSet
  [
  	[table1.offset_a1,table2.offset_b1],
      [table1.offset_a2,table2.offset_b2],
      ...
  ]
  ```

- 多表join：首先进行两表join，得到返回的 HashSet 。之后提取on条件，对 HashSe t进行建表操作。之后操作同两表操作。得到结果如下：

  ```sql
  // HashSet
  [
  	[table1.offset_a1, table2.offset_b1, table3.offset_c1,... ],		[table1.offset_a2, table2.offset_b2, table3.offset_c2,... ],
  	...
  ]
  ```

**时间复杂度分析**：对于m个表的join，需要进行`(m+m-2=2m-2)`次建表操作，在join过程中，需要进行`(m-1)`次合并hash table的操作。

**空间复杂度分析**：hash table只存储数据的offset（int 类型，4Byte），所以所需的内存空间最多为`行数*表数*4`。对于1GB的内存，可以承载的数据量为`行数*表数<2^(28)`。

> 对于natural join、outer join、left outer jion、right outer join实现方式与join相仿

##### 3.3.1.3 性能优化

为了优化项目性能，我们实现了Hash Join与Cache机制。Hash Join详见上节。Cache机制实现方式如下：

- Node-Cache：维护内存中索引树节点Cache，数据结构为Map。记录节点及其offset，以及是否被更改。

  ```s&#39;q&#39;l
  <offset, {isChanged, BPlusTreeNode}> // Node Cache结构
  ```

  在更新节点时，将节点写在内存Cache中，每隔一定时间或者当Cache达到一定数量时将内存中cache写回disk。

- Data-Cache：维护内存中数据Cache，这次与Node-Cache不同，仅仅用于查询，所以不需要写回操作。

#### 3.3.2 parser

SQL的解析使用了语法解析工具 ANTLR4，ANTLR语法使用了官方提供的 SQL 语法(<https://github.com/antlr/grammars-v4/blob/master/sqlite/SQLite.g4>)，并对语法进行了简化，去掉了不计划实现的功能。通过 ANTLR4 对输入的 SQL 语句进行解析得到语法解析树，并使用自动生成的 Visitor 对解析树的节点进行访问并进行相应的操作。

### 3.4 多客户端通信模块

#### 3.4.1 客户端实现

- 利用java实现了简易的基于TCP的服务端和客户端，服务端采用线程池支持多连接。
- 基于java实现客户端GUI设计

#### 3.4.2 共享资源

为了实现多客户端我们需要实现以下两个功能：

- 资源实时同步

  - 数据级别同步：如果不实现内存中Cache，那么我们可以非常简单的实现多客户端的资源同步。但是涉及到内存中的Cache，所以我们需要使得不同的用户共享Cache。因此，我们在Server类中实现静态全局变量Cache，这样就可以实现同步。
  - Table级别的同步：因为一个Database会保存其中的Table list，但是用户A新建数据表之后，用户B无法更新其Database中的Table list。为解决这一问题，我们在根据表名获取Table实例的时候，需要更新Table list。

- 共享资源加锁

  我们基于Java的ReentrantLock对数据进行加锁。我们需要对不同的数据表，加上不同的锁，同样的数据表加上同样的锁。我们使用线程安全的ConcurrentHashMap实现数据结构的设计。

  ```sql
  // ConcurrentHashMap
  <database_name+"@"+table_name, Lock>
  ```

## 4. 重难点与亮点

### 4.1 高效文件存储

我们采用分页结构来实现文件存储，同时实现了碎片整理和溢出处理。

假设直接使用文件进行顺序存储，那么在进行插入删除过程中，会造成很多碎片区域。使用分页存储可以减少碎片，同时省去了维护空闲区域记录表的开销。

### 4.2 多键值索引、多索引树

考虑如下数据表：

```sql 
create table test(id INT, age INT);
```

我们实现了多索引树：即对于数据表 test ，我们可以创建一个以 id 为 key 值的索引树，同时也可以创建一个以 age 为 key 值的索引树。

此外，我们还实现了多键值索引，即对于一个索引树，其键值可以为多个，对于数据表 test ，我们可以创建一个以 id 和 age同时为 key 值的索引。

同时，我们实现了`create index`语句，动态添加索引。这样可以适应多种查询条件，优化查询过程。

### 4.3 完整的查询语句

我们实现了如下的查询语句

```sql
SELECT (*|attrName1 AS name1,attrName2, …attrNameN) FROM tableName WHERE condition;
SELECT (*|tableName1.*|tableName1.AttrName1,tableName1.AttrName2...,tableName2.AttrName1,tableName2.AttrName2,…) FROM tableName1 ((LEFT OUTER JOIN|RIGHT OUTER JOIN|OUTER JOIN|NATURAL JOIN|JOIN) tableName2 ON condition1))* WHERE condition;
```

实现了多表、不同类型的join，支持完整的and/or语句（涉及合取式转析取式）。

我们在多表join过程中使用hash join机制进行优化。在search过程中，使用cache机制进行优化。同时多索引树与多键值索引也可以在一定情况下加快查询。

### 4.4 generator

**动机**：考虑到查询结果有可能非常大，如果一次性返回，可能会占用非常大的内存，甚至爆内存。因此我们考虑使用类似python中的yield方法。然而java原生并不支持yield这种操作。我们在github上找到了一种基于多线程和迭代器的yield实现。(<https://github.com/mherrmann/java-generator-functions>)

**原理**：实现原理基于多线程和java原生的Iterator，可以有效避免返回数据过达造成内存占用过多甚至爆内存的现象。

### 4.5 parser中的重难点

#### 4.5.1 异常处理

由于 ANTLR 对语法错误的默认处理方式会首先尝试修复而非停止解析，就有可能造成解析模块执行错误的 SQL 语句，因此需要修改对错误默认的处理方式。实现方式为实现一个错误处理的类，继承 ANTLR 的 BaseErrorListener，并重写 syntaxError 函数，直接抛出 ParseCancellationException，使解析过程中止。

#### 4.5.2 数据类型封装

当 ANTLR 解析到字面值节点时，该节点对应的数据类型可能为 int, long, float, double 和 string, 但 Visitor 的返回值必须为确定的类型。我们的解决方案是将多种数据类型封装为一个类 DataTypes，类中包括 int, long, float, double, string 和 boolean 变量各一个和一个用来表示数据类型的整数，通过判断数据类型可以得到对应类型的变量。同时，选择表达式中的各种运算操作也都在这个类中实现。

#### 4.5.3 选择表达式实现

实现选择表达式类似于实现一个计算器。每一个查询的 attribute 相当于一个表达式，首先将所有表达式所需要查询的列的集合计算出来，查询得到结果，之后对每一个表达式的解析树自顶向下递归求值，最后返回一个 DataTypes 对象，从中获取计算结果。原理：实现原理基于多线程和java原生的Iterator，可以有效避免返回数据过达造成内存占用过多甚至爆内存的现象。

#### 4.5.4 AND/OR 逻辑处理

我们对查询逻辑的存储使用了三维 Arraylist，其中第一维之间的条件之间的关系为或，第二维之间的条件之间的关系为与，第三维则保存每一个条件(如A>B)。但是在遇到括号时，需要对逻辑进行转换。如果括号由 OR 相连(如(xxx)OR(xxx))，直接将左边和右边的条件的 Arraylist 相连即可。如果括号由 AND 相连，需要将两边第一维(或关系)中的每一个条件两两相连，例如：

```sql
(A OR B) AND (C OR D)
= (A AND (C OR D)) OR (B AND (C OR D))
= (A AND C) OR (A AND D) OR (B AND C) OR (B AND D)
```

### 4.6 多用户机制

我们在服务端实现了多用户支持，其中包括资源同步与资源锁机制。

为了实现同步操作，我们实现了内存中Cache的共享以及database中Table list的更新。

为了实现锁机制，我们使用线程安全的ConcurrentHashMap对不同的数据表进行加锁。并对锁机制进行了测试（详见6.项目测试）。

> 不实现多用户机制的数据库不能称为一个完整的数据库！！！

### 4.7 异常处理

对于客户端关闭的情况，程序会catch到这个异常，然后将cache中的内容写入disk。

为了防止断电、宕机等异常情况，我们使用TimerTasks每隔一定时间将所有cache写回disk。

## 5. 项目运行

环境： `java>=1.11`

### 5.1 服务器

* 启动

  ```shell
  cd out/artifacts/NBSQL_jar
  java -jar Server.jar [port_number]
  ```

  默认端口为59898

### 5.2 客户端

* 启动

  ```shell
  cd out/artifacts/NBSQL_jar2
  java -jar GUIClient.jar 
  ```

* 连接

  * 启动客户端之后会自动弹出连接窗口，输入服务器ip和port即可。
  * Server选项卡可选择Connect和Disconnect连接和关闭连接

* 输入sql语句

  * 在客户端下方文本框中输入sql语句并选择Enter可发送sql指令
  * 左边栏为返回结果（包括提示信息和错误信息）以及时间，右边栏为sql历史记录

* import sql

  * File选项卡中选择import sql，打开sql文件

## 6. 项目测试

我们对项目的功能、性能以及多用户共享资源的同步性、正确性进行测试。

### 6.1 功能测试

在成功运行课程提供的SQL语句，并且得到正确返回结果之外。我们还设计了附加功能测试代码，测试代码详见附录。测试代码经助教验收运行正确。

### 6.2 性能测试

#### 6.2.1 插入性能测试

我们对数据库插入1000，5000和10000条数据的时间进行了测试，并与HSQLDB进行了对比，测试结果如下(单位: ms)：

|       | HSQLDB | NBSQL | NBSQL w/o cache |
| ----- | ------ | ----- | --------------- |
| 1000  | 67.3   | 530.4 | 2095            |
| 5000  | 267    | 3575  | 10136           |
| 10000 | 580.5  | 10235 | 22161           |

测试用表：(a int primary key, b long, c float, d double, e string(10)), HSQLDB中对应数据类型为INTEGER，DOUBLE和VARCHAR(10)。

可见，我们的数据库的性能与 HSQLDB 仍有一定差距，不过加入缓存对性能也有显著的提升。

#### 6.2.2 查询性能测试

我们对数据库查询包含1000，5000和10000条数据的表的时间进行了测试，并与HSQLDB进行了对比，测试结果如下(单位: ms)：

|       | HSQLDB | NBSQL |
| ----- | ------ | ----- |
| 1000  | 31     | 69    |
| 5000  | 95     | 277   |
| 10000 | 170    | 796   |

查询语句：SELECT * FROM TEST WHERE A < (num/2);,num=1000,5000,10000

此外，我们还对create index的效果进行了测试，测试结果如下：

|       | w/ index | w/o index |
| ----- | -------- | --------- |
| 1000  | 203      | 1139      |
| 5000  | 728      | 4853      |
| 10000 | 1347     | 8552      |

查询语句：SELECT * FROM TEST WHERE B < (num/2);,num=1000,5000,10000

可见，为无索引的列添加索引可以使查询速度显著提升。

#### 6.2.3 JOIN性能测试

我们对数据库查询两个包含1000，5000和10000条数据的表join连接的表的时间进行了测试，并与HSQLDB进行了对比，测试结果如下(单位: ms)：

|       | HSQLDB | NBSQL |
| ----- | ------ | ----- |
| 1000  | 78     | 158   |
| 5000  | 206    | 871   |
| 10000 | 324    | 2385  |

查询语句：SELECT * FROM TEST1 JOIN TEST2 ON TEST1.A = TEST2.A

### 6.3 资源锁测试

我们创建数据表，并且插入数据`(id,name) values (id，"test")`。尝试使用两个线程对数据进行update。首先查询出“test”，再进行更改。

```python
class MyThread(threading.Thread):
    def run(self):
        execute "update tb set name='%s' where name='test';"%self.arg
        
connect()
execute("use database test;")
execute("create table tb (id int, name string(32));")

for i in range(100):
    execute("insert into tb values(%d, 'test');"%i)
    
t1 = MyThread("t1")
t2 = MyThread("t2")
t1.start()
t2.start()
```

如果加锁成功，则update过后，数据的name字段均为“t1”或者均为“t2”。

我们的项目通过了这个测试，说明项目的锁机制可以正确运行。

## 附录

### 项目支持的SQL语句

- Create table: CREATE  TABLE tableName(attrName1 Type1,  attrName2 Type2, …, attrNameN TypeN NOT NULL, PRIMARY KEY(attrName1));
- Create index: CREATE INDEX indexName tableName(columnName);
- Drop table: DROP TABLE tableName;
- Show table: SHOW TABLE tableName;
- Insert: INSERT INTO [ tableName (attrName1,attrName2,...,attrNameN)] VALUES (attrValue11, attrValue21,..., attrValueN1),…,(attrValue1n, attrValue2n,..., attrValueNn);
- Select
  - SELECT (*|attrName1 AS name1,attrName2, …attrNameN) FROM tableName WHERE condition;
  - 支持选择表达式（包括加减乘除取余运算，关系比较，AND/OR/NOT）
    - 例如：attrName1+attrName2, (attrName1>10)AND(attrName1<50),...
  - 支持 as alias 
  - where 条件支持 AND/OR和括号
    - 例如：(student_id=4 OR student_id=1) AND (score <= 89 OR score > 95)
- Select with join
  - SELECT (*|tableName1.\*|tableName1.AttrName1,tableName1.AttrName2...,tableName2.AttrName1,tableName2.AttrName2,…) FROM tableName1 ((LEFT OUTER JOIN|RIGHT OUTER JOIN|OUTER JOIN|NATURAL JOIN|JOIN) tableName2 ON condition1))\* WHERE condition;
  - 支持多表join 和 LEFT/RIGHT/FULL outer join(outer不能省略)
  - natural join = 省略 on 条件
  - 对选择表达式，AS，where条件的支持同上
- Delete: DELETE FROM tableName WHERE condition;
- Update: UPDATE tableName SET attrName1= attrValue1, … ,attrNameN=attrValueN  WHERE  condition;
  - update支持同时修改多列
  - delete/update 的 condition 与 select 相同
- Create/Drop/Use/Show DataBase: CREATE/DROP/USE/SHOW DATABASE databaseName;
- Show Databases;

### 附加功能测试代码

```sql
--database相关测试;
create database test2;
create database test3;
show databases;
use database test2;
create table test1(a int primary key, b long not null, c float primary key, d double, e string(10));
create table test2(a int primary key, b long not null, c float primary key, d double, e string(10));
create table test3(a int primary key, b long not null, c float primary key, d double, e string(10));
show table test1;
drop table test2;
show database test2;
--test2正在被使用,无法被drop,此处应提示错误;
drop database test2;
use database test;
drop database test2;
drop database test3;
--database相关测试结束;
create table course(course_id long, title string(50), dept_name string(20), teacher_id long, primary key(course_id));
create table teacher(teacher_id long, name string(20) not null, dept_name string(20), primary key(teacher_id));
create table student(student_id long, name string(20) not null, dept_name string(20), primary key(student_id));
--多列主键;
create table scores(student_id long not null, course_id long not null, score int not null, primary key(student_id,course_id));
--insert同时插入多条数据;
insert into student values (1,'zhao','thss'), (2,'ding','thss'), (3,'wu','thss'), (4,'li','cs'), (5,'zhang','ee'), (6,'tuixue','thss');
--create index;
create index id on student(name);
insert into teacher values (11,'yang','thss'), (12,'liu','thss'), (13,'deng','cs'),(14,'wang','thss'),(15,'wen','thss'), (16,'tuixiu','thss');
insert into course values (101,'csapp','thss',11),(102,'se1','thss',12),(103,'data','cs',13),(104,'db','thss',14),(105,'os','thss',15),(106,'cloud','thss',15),(107,'calculus','math',20);
insert into scores values (1,101,94),(1,102,95),(1,104,100),(1,105,94),(1,106,89);
insert into scores values (2,101,100),(2,102,100),(2,104,100),(2,105,100);
insert into scores values (3,101,100),(3,102,100),(3,104,100),(3,105,100);
insert into scores values (4,102,59),(4,103,9),(4,104,89);
insert into scores values (5,101,60),(5,103,70),(5,106,80);
--primary key重复,插入应失败;
insert into scores values (1,101,100);
--违反not null,插入应失败;
insert into student(student_id,dept_name) values(10,'thss');
--选择表达式,as alias,带括号 AND/OR 测试;
select student_id+(2016*1000000) as id, course_id%100, score, score>=60 as pass, (score+10)>=60 as tiaofen_pass from scores where (student_id=4 OR student_id=1) AND (score <= 89 OR score > 95);
--natural join测试;
select * from teacher natural join course;
--多表join,right outer join,带括号 AND/OR 测试;
select student.name, course.course_id, teacher.name, scores.score from student join scores on student.student_id=scores.student_id join course on scores.course_id=course.course_id right outer join teacher on course.teacher_id=teacher.teacher_id where (teacher.teacher_id=16 OR scores.score>=95) AND (teacher.name='tuixiu' OR scores.score <= 100);
--left outer join测试;
select student.*, scores.* from student left outer join scores on student.dept_name=scores.student_id where student.student_id = 6;
--full outer join测试;
select student.*, course.* from course outer join student on student.dept_name=course.dept_name where course.dept_name <> 'thss';
--update同时修改多列;
update course set dept_name='ss',teacher_id=0 where dept_name='thss' and teacher_id >= 15;
select * from course;
drop table course;
drop table teacher;
drop table student;
drop table scores;
```
