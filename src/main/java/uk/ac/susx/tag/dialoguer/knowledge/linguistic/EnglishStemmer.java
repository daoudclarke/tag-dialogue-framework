package uk.ac.susx.tag.dialoguer.knowledge.linguistic;

import org.tartarus.snowball.SnowballStemmer;
import uk.ac.susx.tag.dialoguer.Dialoguer;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 02/06/2015
 * Time: 15:16
 */
public class EnglishStemmer {

    private SnowballStemmer stemmer;

    public EnglishStemmer() {
        try {
            Class stemClass = Class.forName("org.tartarus.snowball.ext.englishStemmer");
            stemmer = (SnowballStemmer) stemClass.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new Dialoguer.DialoguerException("Could not find stemmer", e);
        }
    }

    public String stem(String word){
        stemmer.setCurrent(word);
        stemmer.stem();
        return stemmer.getCurrent();
    }
}
