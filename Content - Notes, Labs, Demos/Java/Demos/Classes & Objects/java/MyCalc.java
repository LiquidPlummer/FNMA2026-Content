public class MyCalc implements Calculator{
    @Override
    public int add(int a, int b) {
        return a+b;
    }

    public int add(int ...nums) {
        int sum = 0;
        for(int n : nums) {
            sum += n;
        }
        return sum;
    }
}
