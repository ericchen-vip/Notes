# put方法第五阶段-扩容

[TOC]

## 扩容的时机

判断是否需要扩容，也就是当更新后的键值对总数 baseCount >= 阈值 sizeCtl 时，进行 rehash ，这里面会有两个逻辑。

- **如果当前正在处于扩容阶段，则当前线程会加入并且协助扩容**
- **如果当前没有在扩容，则直接触发扩容操作**

![image-20200919131837283](../../../assets/image-20200919131837283.png)

- [addCount后半段-扩容](#addCount后半段-扩容)
- [helpTransfer其他线程协助扩容](#helpTransfer其他线程协助扩容)

## addCount后半段-扩容

```java
if (check >= 0) { //如果 binCount>=0，标识需要检查扩容
  Node<K,V>[] tab, nt; int n, sc;
//标识集合大小，如果集合大小大于或等于扩容阈值(默认值的 0.75) 
//并且 table 不为空并且 table 的长度小于最大容量
  while (s >= (long)(sc = sizeCtl) && (tab = table) != null &&
         (n = tab.length) < MAXIMUM_CAPACITY) {
    int rs = resizeStamp(n);
    if (sc < 0) { //sc<0，也就是sizeCtl<0，说明已经有别的线程正在扩容了
      //这 5 个条件只要有一个条件为 true，说明当前线程不能帮助进行此次的扩容，直接跳出循环
			//sc >>> RESIZE_STAMP_SHIFT!=rs 表示比较高 RESIZE_STAMP_BITS 位 生成戳和 rs 是否相等，相同
			//sc=rs+1 表示扩容结束
			//sc==rs+MAX_RESIZERS 表示帮助线程线程已经达到最大值了
 			//nt=nextTable -> 表示扩容已经结束
			//transferIndex<=0 表示所有的 transfer 任务都被领取完了，没有剩余的 hash 桶来给自己自己好这个线程来做 transfer
      
      if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
          sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
          transferIndex <= 0)
        break;
      if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
        transfer(tab, nt);//当前线程,尝试帮助此次扩容，如果成功，则调用 transfer
    }
    // 如果当前没有在扩容，那么 rs 肯定是一个正数，通过 rs<<RESIZE_STAMP_SHIFT 将 sc 设置 为一个负数，+2 表示有一个线程在执行扩容
    else if (U.compareAndSwapInt(this, SIZECTL, sc,
                                 (rs << RESIZE_STAMP_SHIFT) + 2))
      transfer(tab, null);
    s = sumCount();// 重新计数，判断是否需要开启下一轮扩容
  }
}
```

## helpTransfer其他线程协助扩容

如果对应的节点存在，判断这个节点的 hash 是不是等于 MOVED(-1)，说明当前节点是 ForwardingNode 节点，
意味着有其他线程正在进行扩容，那么当前现在直接帮助它进行扩容，因此调用 helpTransfer 方法

![image-20200722193649153](../../../assets/image-20200722193649153.png)

```java
    final Node<K,V>[] helpTransfer(Node<K,V>[] tab, Node<K,V> f) {
        Node<K,V>[] nextTab; int sc;
      // 判断此时是否仍然在执行扩容,nextTab=null 的时候说明扩容已经结束了
        if (tab != null && (f instanceof ForwardingNode) &&
            (nextTab = ((ForwardingNode<K,V>)f).nextTable) != null) {
            int rs = resizeStamp(tab.length);
            while (nextTab == nextTable && table == tab &&
                   //说明扩容还未完成的情况下不断循环来尝试将当前 线程加入到扩容操作中
                   (sc = sizeCtl) < 0) {
//下面部分的整个代码表示扩容结束，直接退出循环
//transferIndex<=0 表示所有的 Node 都已经分配了线程 //sc=rs+MAX_RESIZERS 表示扩容线程数达到最大扩容线程数
//sc >>> RESIZE_STAMP_SHIFT !=rs， 如果在同一轮扩容中，那么 sc 无符号右移比较高位和 rs 的值，那么应该是相等的。如果不相等，说明扩容结束了 
//sc==rs+1 表示扩容结束
                if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                    sc == rs + MAX_RESIZERS || transferIndex <= 0)
                    break;//跳出循环
                if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {
                 //在低16位 上增加扩容线程数
                    transfer(tab, nextTab); //帮助扩容
                    break;
                }
            }
            return nextTab;
        }
        return table;
    }
```
