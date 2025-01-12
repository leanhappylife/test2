import java.util.*;

public class Solution {

    // 核心求解方法
    public static int strConvert(String str1, String str2) {
        // 若最开始就相同，直接返回0步
        if (str1.equals(str2)) {
            return 0;
        }

        int n = str1.length();
        // 队列中存放 (当前字符串, 当前step)，其中 step 表示“下一步要反转的子串长度=step+1”
        Queue<State> queue = new LinkedList<>();
        // 使用一个哈希集合记录已访问状态，避免死循环
        // “状态”不仅要包含字符串本身，还要包含当前step（或下一次要反转的长度）
        Set<String> visited = new HashSet<>();

        // 从 step=1 开始：表示下一步要反转长度为2的子串
        queue.offer(new State(str1, 1));
        visited.add(str1 + "#1");

        // BFS
        while (!queue.isEmpty()) {
            State current = queue.poll();
            String curStr = current.s;
            int step = current.step;

            // 要反转的子串长度
            int reverseLen = step + 1;
            // 如果反转长度大于 n，就无法继续
            if (reverseLen > n) {
                continue;
            }

            // 枚举所有可以反转的子串的起始位置
            for (int start = 0; start + reverseLen <= n; start++) {
                // 反转 [start, start+reverseLen-1]
                String nextStr = reverseSubstring(curStr, start, start + reverseLen - 1);

                // 如果得到的就是目标字符串，返回当前步数
                if (nextStr.equals(str2)) {
                    return step; // 表明用了 step 步
                }

                // 否则将 (newString, step+1) 入队，继续搜索下一步
                int nextStep = step + 1;
                String stateKey = nextStr + "#" + nextStep;
                if (nextStep <= n && !visited.contains(stateKey)) {
                    visited.add(stateKey);
                    queue.offer(new State(nextStr, nextStep));
                }
            }
        }

        // 搜索完毕仍然无法得到 str2，返回 -1
        return -1;
    }

    // 工具函数：反转字符串 s 在 [left, right] 区间的子串
    private static String reverseSubstring(String s, int left, int right) {
        char[] arr = s.toCharArray();
        while (left < right) {
            char tmp = arr[left];
            arr[left] = arr[right];
            arr[right] = tmp;
            left++;
            right--;
        }
        return new String(arr);
    }

    // 封装队列中的状态
    static class State {
        String s;
        int step; // 表示已经做了 (step-1) 步，下一步的反转长度为 step+1

        State(String s, int step) {
            this.s = s;
            this.step = step;
        }
    }

    // 主函数：读取输入并输出结果
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        // input for str1
        String str1 = in.nextLine();
        // input for str2
        String str2 = in.nextLine();
        in.close();

        int result = strConvert(str1, str2);
        System.out.print(result);
    }
}
