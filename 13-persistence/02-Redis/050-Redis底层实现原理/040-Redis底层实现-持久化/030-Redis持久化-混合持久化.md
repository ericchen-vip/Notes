# 030-Redis持久化-混合持久化

[TOC]

- RDB恢复快,但是会丢失数据
- AOF恢复慢,但是数据不会丢失

所以要混合持久化

Redis 在重启的时候,先加载 rdb 的内容,然后再重放增量 aof ,就可以完全替代之前的 AOF 全量文件重放

<img src="../../../../assets/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3lobF9qeHk=,size_16,color_FFFFFF,t_70.png" alt="img" style="zoom:50%;" />

```
aof-use-rdb-preamble yes
```

