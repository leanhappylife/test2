import java.util.*;
import java.lang.*;
import java.io.*;

public class Solution
{
    // 按层序字符串计算每个工程师(字符'1')所需的最小信号强度
    public static int[] minSignalStrength(String inputString)
    {
        // 将字符串转为字符数组
        char[] arr = inputString.toCharArray();
        int n = arr.length;

        // 收集所有工程师(‘1’)的索引，以便最终结果按出现顺序输出
        List<Integer> engineerIndices = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            if (arr[i] == '1') {
                engineerIndices.add(i);
            }
        }

        // 构建邻接表，仅当arr[i]=='1'时才会连接
        List<List<Integer>> adj = new ArrayList<List<Integer>>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<Integer>());
        }
        for (int i = 0; i < n; i++) {
            if (arr[i] == '1') {
                int left = 2 * i + 1;
                int right = 2 * i + 2;
                if (left < n && arr[left] == '1') {
                    adj.get(i).add(left);
                    adj.get(left).add(i);
                }
                if (right < n && arr[right] == '1') {
                    adj.get(i).add(right);
                    adj.get(right).add(i);
                }
            }
        }

        // 对每个工程师做一次 BFS，找到最远距离(即所需最小信号强度)
        int size = engineerIndices.size();
        int[] answer = new int[size];
        for(int k = 0; k < size; k++) {
            int idx = engineerIndices.get(k);
            answer[k] = bfsMaxDistance(idx, adj, arr);
        }

        return answer;
    }

    // 从指定节点出发做 BFS，计算最远工程师节点的距离
    private static int bfsMaxDistance(int start, List<List<Integer>> adj, char[] arr) {
        int n = arr.length;
        int[] dist = new int[n];
        Arrays.fill(dist, -1);

        Queue<Integer> queue = new LinkedList<Integer>();
        dist[start] = 0;
        queue.offer(start);

        int maxDist = 0;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            // 遍历u的所有相邻节点
            for (int v : adj.get(u)) {
                if (dist[v] == -1) {
                    dist[v] = dist[u] + 1;
                    queue.offer(v);
                    if (dist[v] > maxDist) {
                        maxDist = dist[v];
                    }
                }
            }
        }
        return maxDist;
    }

    // 主函数，读取输入并输出结果
    public static void main(String[] args)
    {
        Scanner in = new Scanner(System.in);
        String inputString = in.nextLine();  // 读取一行输入
        in.close();

        int[] result = minSignalStrength(inputString);

        // 依题目格式：用空格拼接输出(最后一个值后面无多余空格)
        for(int idx = 0; idx < result.length - 1; idx++)
        {
            System.out.print(result[idx] + " ");
        }
        System.out.print(result[result.length - 1]);
    }
}
