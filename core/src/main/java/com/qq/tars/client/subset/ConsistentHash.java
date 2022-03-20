package com.qq.tars.client.subset;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHash {

    SortedMap<Integer, String> virtualSubset;
    RatioConfig ratioConfig;
    SortedMap.Entry<Integer,String> entrySubset;

    public ConsistentHash() {
    }

    public void setVirtualSubset(Map<String, Integer> rules){
        SortedMap<Integer, String> virtualSubset = new TreeMap<Integer, String>();
        for(String subset : rules.keySet()){
            for(int i = 0; i < rules.get(subset); i++){
                String virtualSubsetName = subset + "&&" + String.valueOf(i);
                int hash = getHash(virtualSubsetName);
                virtualSubset.put(hash, virtualSubsetName);
            }
        }
        this.virtualSubset = virtualSubset;
    }

    public String getSubsetByVirtual(String routeKey){
        //得到该key的hash值
        int hash = getHash(routeKey);
        //得到大于该Hash值的所有Map
        SortedMap<Integer, String> subMap = virtualSubset.tailMap(hash);
        if(subMap.isEmpty()){
            //如果没有比该key的hash值大的，则从第一个node开始
            Integer i = virtualSubset.firstKey();
            //返回对应的服务器
            return virtualSubset.get(i).split("&&")[0];
        }else{
            //第一个Key就是顺时针过去离node最近的那个结点
            Integer i = subMap.firstKey();
            //返回对应的服务器
            return subMap.get(i).split("&&")[0];
        }
    }

    //使用FNV1_32_HASH算法计算服务器的Hash值,这里不使用重写hashCode的方法，最终效果没区别
    private static int getHash(String str){
        final int p = 16777619;
        int hash = (int)2166136261L;
        for (int i = 0; i < str.length(); i++){
            hash = (hash ^ str.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        // 如果算出来的值为负数则取其绝对值
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }
}
