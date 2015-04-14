package uk.ac.susx.tag.dialoguer.knowledge.database.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Stores a Product and other information about it (such as how closely it matches the user query)
 *
 *
 * Created by juliewe on 30/09/2014.
 */
public class EnhancedProduct {

    private final Product product;
    private List<Double> scores;
    private List<String> matches; //a record of the matches made that correspond to the scores for further analysis

    public static double simThreshold = 0.5;

    public EnhancedProduct(Product product) {
        this.product=product;
        this.scores=new ArrayList<>();
        this.matches=new ArrayList<>();
    }

    public Product getProduct(){
        return product;
    }

    public double getAverageScore(){
        double total=0.0;
        for(Double sc:scores){
            total+=sc;
        }
        return total/scores.size();
    }

    private void addScore(double sc,String match){
        scores.add(sc);
        matches.add(match);
    }

    public void match(String word, String wordtype, String phrasetype){
        switch(phrasetype){
            case("NP"):
                switch(wordtype){
                    case("head"):
                        headProductMatch(word);
                        break;
                    case("mod"):
                        modProductMatch(word);
                        break;
                    default:
                        System.err.println("Unknown wordtype: "+wordtype);
                }
                break;
            case("PP"):
                switch(wordtype){
                    case("head"):
                        headContributorMatch(word);
                        break;
                    case("mod"):
                        modContributorMatch(word);
                        break;
                    default:
                        System.err.println("Unknown wordtype: "+wordtype);

                }
                break;
            default:
                System.err.println("Unknown phrasetype: "+phrasetype);

        }

    }

    private void headProductMatch(String word){
        //match word against product tags and properties
        List<String> tags = product.getTags();
        double sim=0;
        double highest=0;
        String match="";
        for (String tag:tags){
            sim=similarity(word,tag);
            if (sim>highest){
                highest=sim;
                match=word+":tag:"+tag;

            }
        }
        //need to do properties as well
        Set<String> propertyTypes = product.getProperties().keySet();
        List<String> propertyValues = new ArrayList<>();
        for(String prop: propertyTypes){
            propertyValues.addAll(product.fetchPropertyValues(prop));
        }
        for (String value:propertyValues){
            sim = similarity(word, value);
            if(sim > highest){
                highest=sim;
                match = word+":prop:"+value;
            }
        }


        addScore(highest,match);

    }
    private void modProductMatch(String word){
        //could treat modifiers differently to heads but lets not bother at the moment
        headProductMatch(word);
    }
    private void headContributorMatch(String word){
        //could treat modifiers differently to heads but lets not bother at the moment
        headProductMatch(word);
    }
    private void modContributorMatch(String word){
        //could treat modifiers differently to heads but lets not bother at the moment
        headProductMatch(word);
    }

    private static double similarity(String word1, String word2){
        //this should probably be some kind of lexical /semantic/ distributional similarity but lets start off with string overlap
        return stringoverlap(word1, word2);
    }

    private static double stringoverlap(String w1, String w2){
        w1=w1.toLowerCase().trim()+"%";//add random characters at end which won't match
        w2=w2.toLowerCase().trim()+"$";
        double best=0;
        double current=0;
        int offset=0;
        for(int i=0;i<w1.length();i++){
            for(int j=i+offset;j<w2.length();j++){
                //System.err.print(i+" - "+j+":");
                if(w1.charAt(i)==w2.charAt(j)){
                    //System.err.println(" match");
                    current+=1;
                    offset=j-i;
                    break;

                } else{
                    //System.err.println(" do not match");
                    if(current>best){
                        best=current;
                    }
                    offset=-(i+1);
                    current=0;
                }
            }

        }
        //System.err.println(best);
        return best/(w1.length()-1);
    }

    public static void testoverlap(){

        String s1="bluray";
        String s2="blu-ray";
        String s3="blu-rsy";

        displaysim(s1,s2);
        displaysim(s1,s3);
        displaysim(s2,s3);
        displaysim(s3,s2);
        displaysim(s3,s1);
        displaysim(s2,s1);

    }
    private static void displaysim(String s1, String s2){
        System.out.println(s1+" "+s2+" "+stringoverlap(s1,s2));
    }
}
