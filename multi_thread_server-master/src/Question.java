import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Question {

    private String question;
    private String answer;

    public Question(String q, String answer){
        this.question = q;
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public String getQuestion() {
        return question;
    }

    public static ArrayList<Question> getQuestions(){
        ArrayList<Question> list = new ArrayList<>();
        list.add(new Question("What is your favourite color?", "red"));
        list.add(new Question("Last name of your best friend?", "bozyilan"));
        list.add(new Question("your group name?", "superstars"));
        list.add(new Question("Who is your favourite teacher?", "öznur özkasap"));

        return list;

    }
}
