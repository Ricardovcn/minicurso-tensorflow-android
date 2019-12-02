package mariannelinhares.mnistandroid.models;
import android.content.res.AssetManager;
//Reads text from a character-input stream, buffering characters so as to provide for the efficient reading of characters, arrays, and lines.
import java.io.BufferedReader;
//for erros
import java.io.IOException;
//An InputStreamReader is a bridge from byte streams to character streams:
// //It reads bytes and decodes them into characters using a specified charset.
// //The charset that it uses may be specified by name or may be given explicitly, or the platform's default charset may be accepted.
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
//made by google, used as the window between android and tensorflow native C++
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class TensorFlowClassifier implements Classifier {

    //Limite para retornar uma previsoa é de 10%, ou seja, somente se existir mais de 10% de chance de ser um numero ele será retornado.
    private static final float THRESHOLD = 0.1f;

    //Interface para comunicação entre SDK e o NDK. jAVA e C++
    private TensorFlowInferenceInterface tfHelper;

    private String name;
    private String inputName;
    private String outputName;
    private int inputSize;
    private boolean feedKeepProb;

    private List<String> labels;
    private float[] output;
    private String[] outputNames;

   // Lendo os labels// Tegerts do arquivo de texto.
    private static List<String> readLabels(AssetManager am, String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(am.open(fileName)));

        String line;
        List<String> labels = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            labels.add(line);
        }
        br.close();
        return labels;
    }


    //Criando e setando as variaveis necessárias para carregar o modelo.
    public static TensorFlowClassifier create(AssetManager assetManager, String name,
            String modelPath, String labelFile, int inputSize, String inputName, String outputName,
            boolean feedKeepProb) throws IOException {
        //intialize a classifier
        TensorFlowClassifier c = new TensorFlowClassifier();

        //Nome do modelo
        c.name = name;
        c.inputName = inputName;
        c.outputName = outputName;



        //Lendo os labels
        c.labels = readLabels(assetManager, labelFile);

        //definindo o caminho do modelo.
        c.tfHelper = new TensorFlowInferenceInterface(assetManager, modelPath);
        int numClasses = 10;


        //htamanho da entrada
        c.inputSize = inputSize;

        c.outputNames = new String[] { outputName };

        c.outputName = outputName;
        //Numero de classes
        c.output = new float[numClasses];

        //Quer usar probabilidade? Se sim deve estar como true.
        c.feedKeepProb = feedKeepProb;

        return c;
    }


    //Função que faz o reconhecimento.
    @Override
    public Classification recognize(final float[] pixels) {

        //Usando a interface.
        tfHelper.feed(inputName, pixels, 1, inputSize, inputSize, 1);

        //Probabilidades
        if (feedKeepProb) {
            tfHelper.feed("keep_prob", new float[] { 1 });
        }


        //Pegando os possíveis resultados.
        tfHelper.run(outputNames);
        tfHelper.fetch(outputName, output);


        //Percorrendo o vetor de resultados e pegando o de meior probabilidade
        // que seja maior que 10%,  definido pelo treshhold.
        Classification ans = new Classification();
        for (int i = 0; i < output.length; ++i) {
            System.out.println(output[i]);
            System.out.println(labels.get(i));
            if (output[i] > THRESHOLD && output[i] > ans.getConf()) {
                ans.update(output[i], labels.get(i));
            }
        }

        //Retorna a melhor classificação.
        return ans;
    }

    @Override
    public String name() {
        return name;
    }
}
