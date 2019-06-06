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
drop database test2;
use database test;
drop database test2;
drop database test3;
create table course(ID long, title string(50), dept_name string(20), teacher_id long, primary key(ID));
create table teacher(ID long, name string(20) not null, dept_name string(20), primary key(ID));
create table student(ID long, name string(20) not null, dept_name string(20), primary key(ID));
create table scores(student_id long not null, course_id long not null, score int not null, primary key(student_id,course_id));
insert into student values (1,'zhao','thss'), (2,'ding','thss'), (3,'wu','thss'), (4,'li','cs'), (5,'zhang','ee'), (6,'tuixue','thss');
create index id on student(name);
insert into teacher values (11,'yang','thss'), (12,'liu','thss'), (13,'deng','cs'),(14,'wang','thss'),(15,'wen','thss'), (16,'tuixiu','thss');
insert into course values (101,'csapp','thss',11),(102,'se1','thss',12),(103,'data','cs',13),(104,'db','thss',14),(105,'os','thss',15),(106,'cloud','thss',15),(107,'calculus','math',20);
insert into scores values (1,101,94),(1,102,95),(1,104,100),(1,105,94),(1,106,89);
insert into scores values (2,101,100),(2,102,100),(2,104,100),(2,105,100);
insert into scores values (3,101,100),(3,102,100),(3,104,100),(3,105,100);
insert into scores values (4,102,59),(4,103,9),(4,104,89);
insert into scores values (5,101,60),(5,103,70),(5,106,80);
select * from scores where (student_id=4 OR student_id=1) AND (score <= 89 OR score > 95);
select student.name, course.id, teacher.name, scores.score from student left outer join scores on student.ID=scores.student_id join course on scores.course_id=course.ID right outer join teacher on course.teacher_id=teacher.ID where (teacher.ID=16 OR scores.score>=95) AND (teacher.name='tuixiu' OR scores.score <= 100);
--insert fail: primary key
insert into scores values (1,101,100);
--insert fail: not null
insert into student(ID,dept_name) values(10,'thss');
select student.*, scores.* from student left outer join scores on student.dept_name=scores.student_id where student.id = 6;
select student.*, course.* from course outer join student on student.dept_name=course.dept_name where course.dept_name <> 'thss';
select student_id+(2016*1000000) as id, course_id%100, score, score>=60 as pass, (score+10)>=60 as tiaofen_pass from scores where (student_id=4 OR student_id=1) AND (score <= 89 OR score > 95);
drop table course;
drop table teacher;
drop table student;
drop table scores;
--create table avengers(id			int not null, name			string(32) not null, power	int not null,weight     float,primary key (ID));
--drop table avengers;
--create table avengers(id			int not null, name			string(32) not null, power	int not null,weight     float,height     double,primary key (ID));
--INSERT INTO avengers VALUES (10, 'Captain', 50, 78.1, 1.85);
--INSERT INTO avengers VALUES (3, 'Thor', 90, 92.1, 1.89);
--INSERT INTO avengers VALUES (7, 'IronMan', 85, 82.1, 1.76);
--INSERT INTO avengers VALUES (4, 'rocket', 40, 42.1, 0.76);
--INSERT INTO avengers VALUES (5, 'Groot', 10, 182.1, 2.76);
--DELETE FROM avengers WHERE name = 'Groot';
--UPDATE avengers SET power = 100 WHERE name = 'Captain';
--create table villain(id			int not null, name			string(32) not null, power	int not null,primary key (ID));
--INSERT INTO villain VALUES (1, 'Thanos', 100);
--INSERT INTO villain VALUES (2, 'Red Skull', 40);
--INSERT INTO villain VALUES (3, 'Hella', 90);
--INSERT INTO villain VALUES (4, 'monster', 10);
--show table avengers;
--select avengers.name, villain.name, villain.power from avengers outer join villain on avengers.power = villain.power;
--select * from avengers;
--select id, name from avengers where id = 4;
--drop table avengers;
--drop table villain;
quit