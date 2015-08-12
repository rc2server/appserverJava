
insert into rcuser (id,login,email,firstname,lastname,admin,passworddata) values (1,'test','cornholio@stat.wvu.edu','Great','Cornholio', false, '$2a$10$eXG/iPfli9q0RMO6TfuLVeYkyd02.U8SMhaeeBv1aVyBBYh8ZCFI6');
insert into rcworkspace (id, userid, name) values (1, 1, 'foofy');
insert into rcworkspace (id, userid, name) values (2, 1, 'thrice');
COPY rcfile (id, wspaceid, name, datecreated, lastmodified, version, filesize, objtype) FROM stdin;
1	1	sample.R	2015-08-12 15:23:25.827231	2015-08-12 15:23:25.827231	0	28	file
2	1	foo.R	2015-08-12 15:23:25.853145	2015-08-12 15:23:25.853145	0	28	file
\.
COPY rcfiledata (id, bindata) FROM stdin;
1	\\x73616d706c6558203c2d206328312c332c34290a73616d706c65580a
2	\\x73616d706c6558203c2d206328312c332c34290a73616d706c65580a
\.

