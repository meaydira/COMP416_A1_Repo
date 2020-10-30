
import sun.misc.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

class ServerThread extends Thread
{
    protected DataInputStream is;
    protected DataOutputStream os;
    protected Socket s;
    private String line = new String();
    private String lines = new String();
    Charset charset = Charset.forName("ISO-8859-1");
    final byte[] buf = new byte[4096];


    /**
     * Creates a server thread on the input socket
     *
     * @param s input socket to create a thread on
     */
    public ServerThread(Socket s)
    {
        this.s = s;
    }

    /**
     * The server thread, echos the client until it receives the QUIT string from the client
     */
    public void run()
    {
        try
        {
            is = new DataInputStream( s.getInputStream()) ;

          //  os = new PrintWriter(s.getOutputStream());
            os = new DataOutputStream(s.getOutputStream());



           // bos = new ByteArrayOutputStream(s.getInputStream());
            // WritableByteChannel oc = Channels.newChannel(s.getOutputStream());
           // ReadableByteChannel ic = Channels.newChannel(s.getInputStream());


        }
        catch (IOException e)
        {
            System.err.println("Server Thread. Run. IO error in server thread");
        }

        System.out.println("Authenticating the client: " + s.getRemoteSocketAddress());

        if(!isAuth()){
            String text = "Authentication failed, closing the connection";
            byte phase = 0;
            byte type = 3; // auth_failed;
            try {
                Message.sendMessage(os, new Message(phase,type, text.getBytes(charset).length ,text));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //os.println("Authentication failed, closing the connection");
            //os.flush();
            closeConnection();
        }else{
            String text = "Authentication successful";
            byte phase = 0;
            byte type = 4; // auth_success;
            try {
                Message.sendMessage(os, new Message(phase,type, text.getBytes(charset).length ,text));
            } catch (IOException e) {
                e.printStackTrace();
            }

            // os.println("Authentication successful");
           // os.flush();
        }

        try
        {
            line = is.readLine();
            while (line.compareTo("QUIT") != 0)
            {
		lines = "Client messaged : " + line + " at  : " + Thread.currentThread().getId();
                //os.println(lines);
                //os.flush();
                System.out.println("Client " + s.getRemoteSocketAddress() + " sent :  " + lines);
                line = is.readLine();
            }
        }
        catch (IOException e)
        {
            line = this.getName();
            System.err.println("Server Thread. Run. IO Error/ Client " + line + " terminated abruptly");
        }
        catch (NullPointerException e)
        {
            line = this.getName();
            System.err.println("Server Thread. Run.Client " + line + " Closed");
        } finally
        {
            closeConnection();
        }//end finally
    }


    private void closeConnection(){
        try
        {
            System.out.println("Closing the connection");
            if (is != null)
            {
                is.close();
                System.err.println(" Socket Input Stream Closed");
            }

            if (os != null)
            {
                os.close();
                System.err.println("Socket Out Closed");
            }
            if (s != null)
            {
                s.close();
                System.err.println("Socket Closed");
            }

        }
        catch (IOException ie)
        {
            System.err.println("Socket Close Error");
        }
    }


    private boolean isAuth(){

        try
        {
            ArrayList<Question> questions = Question.getQuestions();
            int random = (int) (Math.random() % questions.size());
            Question question1 = questions.get(random);
            questions.remove(random);
            random = (int) (Math.random() % questions.size());
            Question question2 = questions.get(random);
            questions.remove(random);

            String text = question1.getQuestion();
            byte phase = (byte)0;
            byte type = (byte)2; // auth_challenge;

                Message.sendMessage(os, new Message(phase,type, text.getBytes(charset).length ,text));

           // os.println(question1.getQuestion());
           // os.flush();
           // line = is.readLine();

           /* if ((line = is.readLine()) == null) {
               System.out.println("Readline returned empty string");
                //Your code
            }  */
           Message client_response = Message.nextMessageFromSocket(is);

            System.out.println("Client answered question 1: " + question1.getQuestion() + " answer :  " + client_response.payload);

            String response = "";
            if(client_response.payload.equalsIgnoreCase(question1.getAnswer())){
                System.out.println(" Question 1 is Correct");
                response+= "Question 1 is Correct ! One more : ";

            }else{
                System.out.println("Wrong! correct answer was: " + question1.getAnswer());

                //  os.println("Wrong answer, closing the connection ");
              //  os.flush();
                return false;
            }

            response+=question2.getQuestion();
            phase = 0;
            type = 2; // auth_challenge;

            Message.sendMessage(os, new Message(phase,type, response.getBytes(charset).length ,response));
            //os.println(response);
            //os.flush();
            //line = is.readLine();
            client_response = Message.nextMessageFromSocket(is);
            System.out.println("Client answered question 2: " + question2.getQuestion() + " answer :  " + client_response.payload);

            if(client_response.payload.equalsIgnoreCase(question2.getAnswer())){
                System.out.println(" Question 2 is Correct");
                phase = 0;
                type = 4; // auth_success;
                response = "Question 2 is Correct ! User is authenticated successfully";
                Message.sendMessage(os, new Message(phase,type, response.getBytes(charset).length ,response));
               // os.println("Question 2 is Correct ! User is authenticated successfully ");
                // os.flush();
                return true;

            }else{

                System.out.println("Wrong! correct answer was: " + question1.getAnswer());
                //os.println("Wrong answer");
                //os.flush();
                return false;
            }






        }
        catch (IOException e)
        {
            line = this.getName();
            System.err.println("Server Thread. Run. IO Error/ Client " + line + " terminated abruptly");
        }
        catch (NullPointerException e)
        {
            line = this.getName();
            System.err.println("Server Thread. Run.Client " + line + " Closed");
        } finally
        {
            closeConnection();
        }//end finally
        return true;
    }




}
