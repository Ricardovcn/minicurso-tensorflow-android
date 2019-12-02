package mariannelinhares.mnistandroid.models;

public class Classification {

    //Probabilidade
    private float conf;
    // Previsão
    private String label;
    Classification() {
        this.conf = -1.0F;
        this.label = null;
    }
    void update(float conf, String label) {
        this.conf = conf;
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
    public float getConf() {
        return conf;
    }
}
