--create database test2;
--show databases;
--use database test2;
--create table test1(a int primary key, b long not null, c float primary key, d double, e string(10));
--create table test2(a int primary key, b long not null, c float primary key, d double, e string(10));
--create table test3(a int primary key, b long not null, c float primary key, d double, e string(10));
--show table test1;
--drop table test2;
--show database test2;
--drop database test2;
--use database test;
--drop database test2;
--create table test1(a int primary key, b long not null, c float primary key, d double, e string(10));
--insert into test1 values(1,1,1,1,'abc');
--insert into test1 (a,b,c) values(0,0,0);
--insert into test1 (a,c,d,e) values(2,2,2,'abc');
--insert into test1 (b,c,d,e) values(1,1,1,'abc');
--insert into test1 values (1,1,1,1,'abc');
--insert into test1 values (3,1,1,1,'abc');
--insert into test1 values (1,1,3,1,'abc');
----insert into test1 values(2,2,2,2,'abcabcabcabc');
--insert into test1 values (2,2,2,2,'abc'),(3,3,3,3,'abc'),(4,4,4,4,'abc'),(5,5,5,5,'def'),(6,6,6,6,'def');
--select * from test1;
--update
----select a+2, b*3, (b+c)*a as f, d/2 from test1;
----select * from test1 where (a < 3 OR b >=4) AND (e <> 'def' OR d=6);
--create table test2(a int primary key, b long, c float, d double, e string(10));
--insert into test2 values (2,2,2,2,'abc'),(3,3,3,3,'abc'),(4,4,4,4,'abc'),(5,5,5,5,'def'),(6,6,6,6,'def');
--create table test3(a int primary key, b long, c float, d double, e string(10));
--insert into test3 values (2,2,2,2,'abc'),(3,3,3,3,'abc'),(4,4,4,4,'abc'),(5,5,5,5,'def'),(6,6,6,6,'def');
----select test1.a, test2.a from test1 join test2 on test1.a=test2.a;
--drop table test1;
--drop table test2;
--drop table test3;
create table avengers(id			int not null, name			string(32) not null, power	int not null,weight     float,primary key (ID));

drop table avengers;

create table avengers(id			int not null, name			string(32) not null, power	int not null,weight     float,height     double,primary key (ID));

INSERT INTO avengers VALUES (10, 'Captain', 50, 78.1, 1.85);
INSERT INTO avengers VALUES (3, 'Thor', 90, 92.1, 1.89);
INSERT INTO avengers VALUES (7, 'IronMan', 85, 82.1, 1.76);
INSERT INTO avengers VALUES (4, 'rocket', 40, 42.1, 0.76);
INSERT INTO avengers VALUES (5, 'Groot', 10, 182.1, 2.76);

DELETE FROM avengers WHERE name = 'Groot';

UPDATE avengers SET power = 100 WHERE name = 'Captain';

create table villain(id			int not null, name			string(32) not null, power	int not null,primary key (ID));

INSERT INTO villain VALUES (1, 'Thanos', 100);
INSERT INTO villain VALUES (2, 'Red Skull', 40);
INSERT INTO villain VALUES (3, 'Hella', 90);
INSERT INTO villain VALUES (4, 'monster', 10);

show table avengers;

select avengers.name, villain.name, villain.power from avengers join villain on avengers.power = villain.power where villain.power > 40;

select * from avengers;

select id, name from avengers where id = 4;
quit