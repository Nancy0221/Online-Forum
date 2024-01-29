package com.zcx.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "???";

    // 创建根节点
    private TrieNode rootNode = new TrieNode();

    // @PostConstruct：表示这是一个初始化方法，当容器实例化这个bean（在服务启动的时候被初始化）之后，在调用了这个构造器之后，这个方法就会被自动调用
    @PostConstruct
    public void init() {
        try(    
                // this.getClass().getClassLoader()：从类路径（target -> classes）获取类加载器
                // 加载一个字节流（要关闭，因此让try catch自动加上finally并关闭它）
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                // 字节流转换成字符流，再转成缓冲流，读取字符的效率较高
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        ) {
            // 每次读取到的词
            String keyword = null;
            // 一行一个敏感词
            while ((keyword = bufferedReader.readLine()) != null) {
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            throw new RuntimeException("加载敏感词文件失败，服务器出现异常");
        }

    }

    // 将一个敏感词添加到前缀树中
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i ++) {
            char c = keyword.charAt(i);
            // 查看当前节点有没有c这个子节点
            TrieNode subNode = tempNode.getSubNode(c);
            // 没有的话就加进去
            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            // 下移指针
            tempNode = subNode;
            // 标记该单词已经结束了
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    // 过滤敏感词：返回替换掉之后的字符串
    public String filter(String text) {
        // 文本是空的，直接return null
        if (StringUtils.isBlank(text)) {
            return null;
        }
        // 指针1：指向树
        TrieNode tempNode = rootNode;
        // 指针2：指向非法字符串的首位；只会往后移动
        int begin = 0;
        // 指针3：指向非法字符串的最后一位；可能会小范围来回移动：如果该位字符不能组成非法string，就回退到begin重新检查
        int position = 0;
        // 存储结果
        StringBuilder stringBuilder = new StringBuilder();
        // 以指针2为循环条件
        while(begin < text.length()) {
            char c = text.charAt(position);
            // 跳过符号
            if (isSymbol((c))) {
                // 若指针1处于根节点，将此符号计入结果，让指针2向后走一步
                if (tempNode == rootNode) {
                    stringBuilder.append(c);
                    begin ++;
                }
                // 无论符号在开头或中间，指针3都向后走一步
                position ++;
                if (position >= text.length() - 1) {
                    if ((begin >= text.length())) {
                        break;
                    }
                    stringBuilder.append(text.charAt(begin));
                    begin ++;
                    position = begin;
                    tempNode = rootNode;
                }
                continue;
            }
            // 该字符不是符号，检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                stringBuilder.append(text.charAt(begin));
                // 进入下一个位置
                begin ++;
                position = begin;
                // 重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词，将begin到position的字符串替换掉
                stringBuilder.append(REPLACEMENT);
                // 进入下一个位置
                position ++;
                begin = position;
                // 重新指向根节点
                tempNode = rootNode;
            } else if (position >= text.length() - 1) {
                // position超边界了都没发现敏感词，说明以begin开头的字符串不是敏感词
                stringBuilder.append(text.charAt(begin));
                begin ++;
                position = begin;
                tempNode = rootNode;
            } else {
                if (position < text.length() - 1) {
                    // tempNode不是null，说明在树中有匹配的字符，就继续往下查，因此要更新position
                    position ++;
                }
            }
        }
        // stringBuilder.append(text.substring(begin)); ？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？
        return stringBuilder.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character character) {
        // CharUtils.isAsciiAlphanumeric(character)：判断是否为普通字符（是 = true）
        // character < 0x2E80 || character > 0x9FFF：不在东亚文字范围之内
        // 是特殊字符且不是东亚文字，就返回true
        return !CharUtils.isAsciiAlphanumeric(character) && (character < 0x2E80 || character > 0x9FFF);
    }

    // 前缀树
    private class TrieNode {

        // 描述关键词结束标识
        private boolean isKeywordEnd;

        // 子节点（key: 下级字符；value: 下级节点）
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character character, TrieNode trieNode) {
            subNodes.put(character, trieNode);
        }

        // 获取子节点
        public TrieNode getSubNode(Character character) {
            return subNodes.get(character);
        }
    }

}
