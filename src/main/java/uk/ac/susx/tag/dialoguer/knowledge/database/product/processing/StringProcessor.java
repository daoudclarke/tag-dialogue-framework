package uk.ac.susx.tag.dialoguer.knowledge.database.product.processing;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 20/10/2014
 * Time: 15:31
 */
public class StringProcessor {

    public static boolean stringMatch(String value1, String value2) { return stringMatch(value1, value2, 5); }

    public static boolean stringMatch(String value1, String value2, int minLength){
        boolean answer=false;
        if(value1.equals(value2)){ //exact match
            answer=true;
        } else{ //contains match
            //System.err.println(value1.length()+" : "+value2.length());
            if(value1.length()>=minLength && value2.length()>=minLength &&(value1.contains(value2) || value2.contains(value1))){
                answer=true;
            } else{ // editDistance match
                int editDistance = editDistance(value1, value2, minLength);
                double coverage = (double) editDistance / Math.min(value1.length(), value2.length());
                if(coverage<=0.5){answer=true;}
            }
        }
        return answer;
    }

    public static int editDistance(String stringA, String stringB) { return editDistance(stringA, stringB, 5); }

    // see http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
    public static int editDistance(String stringA, String stringB, int minLength) {
        int lengthA = stringA.length();
        int lengthB = stringB.length();

        if (lengthA < minLength || lengthB < minLength ) {                      //guard. Ignore short words
            return 999;
        }

        int[][] distance = new int[lengthA + 1][lengthB + 1];

        for (int i = 0; i <= lengthA; i++)
            distance[i][0] = i;
        for (int j = 1; j <= lengthB; j++)
            distance[0][j] = j;

        for (int i = 1; i <= lengthA; i++)
            for (int j = 1; j <= lengthB; j++)
                distance[i][j] = min3way(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1,
                        distance[i - 1][j - 1] +
                                ((stringA.charAt(i - 1) == stringB.charAt(j - 1)) ? 0 : 1));

        return distance[lengthA][lengthB];
    }
    // see http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
    private static int min3way(int x, int y, int z) {
        return Math.min(Math.min(x, y), z);
    }
}
