package Builder;

public class Client {
    public static void main(String[] args) {

        MealBuilder mb =(MealBuilder)XMLUtil.getBean();

        KFCWaiter waiter = new KFCWaiter();

        waiter.setMealBuilder(mb);

        Meal meal = waiter.construct();

        System.out.println("套餐组成：");
        System.out.println(meal.getFood());
        System.out.println(meal.getDrink());
    }
}
