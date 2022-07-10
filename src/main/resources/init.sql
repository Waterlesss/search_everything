
create table if not exists file_meta (
    name varchar(50) not null,
    path varchar(100) not null,
    is_directory boolean not null,
    size bigint ,
    last_modified timestamp not null,
    pinyin varchar(200),
    pinyin_first varchar(20)
);

insert into file_meta(name,path,is_directory,size,last_modified)
values ("测试.txt","/测试/测试.txt",false,100,1000);