package bu.edu.ec500.sshealthapp;

import java.util.Locale;

public class RecognizedActivity {

    public final static int WALKING = 0;

    public final static int STANDING = 1;

    public final static int WALKING_UP = 2;

    public final static int WALKING_DOWN = 3;

    public final static int SITTING = 4;

    public final static int LYING = 5;

    private int activityType;

    private double possibility;

    public RecognizedActivity() { this(STANDING, 1); }

    public RecognizedActivity(final int activityType, final double activityProb) {
        this.activityType = activityType;
        this.possibility = activityProb;
    }

    public int getActivityType(){
        return this.activityType;
    }

    public double getActivityPossibility(){
        return this.possibility;
    }

    @Override
    public String toString(){
        return String.format(Locale.ENGLISH, "%s : %s", RecognizedActivity.getActivityNameByType(activityType), String.valueOf(possibility));
    }

    public static String getActivityName(final int actType){
        return getActivityNameByType(actType);
    }

    private static String getActivityNameByType(final int type){

        String result = "UNKNOWN";
        switch (type){
            case 0:
                result = "WALKING";
                break;
            case 1:
                result = "STANDING";
                break;
            case 2:
                result = "WALKING_UP";
                break;
            case 3:
                result = "WALKING_DOWN";
                break;
            case 4:
                result = "SITTING";
                break;
            case 5:
                result = "LYING";
                break;
            default:
                break;
        }
        return result;
    }

    public static double getActivityCalorisByType(final int type){

        double result = 0;
        switch (type){
            case 0:
                result = 0.17;
                break;
            case 1:
                result = 0.1;
                break;
            case 2:
                result = 0.15;
                break;
            case 3:
                result = 0.25;
                break;
            case 4:
                result = 0.05;
                break;
            case 5:
                result = 0.08;
                break;
            default:
                break;
        }
        return result;
    }
}
