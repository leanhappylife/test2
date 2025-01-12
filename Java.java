import java.util.*;
import java.lang.*;
import java.io.*;

public class Solution {
    // 方法：统计字符串中单词出现次数（区分大小写后的一致性处理、可重叠）
    public static int countWords(String sentence, String word) {
        // 转小写，保证大小写不敏感
        String lowerSentence = sentence.toLowerCase();
        String lowerWord = word.toLowerCase();

        int answer = 0;
        int index = 0;

        // 只要从 index 开始还能容纳下 word，就继续查找
        while (index <= lowerSentence.length() - lowerWord.length()) {
            // 取出与 word 等长的子串，与目标单词比较
            String sub = lowerSentence.substring(index, index + lowerWord.length());
            if (sub.equals(lowerWord)) {
                answer++;
                // 为了统计“重叠”出现，index 只增加 1
                index++;
            } else {
                // 若不匹配，继续往后移动
                index++;
            }
        }

        return answer;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        // 读取第一行输入：句子
        String sentence = in.nextLine();
        // 读取第二行输入：单词
        String word = in.nextLine();

        int result = countWords(sentence, word);
        System.out.print(result);
    }
}
