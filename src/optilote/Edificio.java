package optilote;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

public class Edificio {

    int id,freq;
    String nome;

    Edificio(){
        this.id = -1;
        this.freq = 0;
        this.nome = null;
    }

    Edificio(int id,int freq,String nome){
        this.id = id;
        this.freq = freq;
        this.nome = nome;
    }
    
    public int getId() {
        return this.id;
    }

    public int getFrequencia() {
        return this.freq;
    }
    
    public String getNome(){
        return nome;
    }
    
    public void setId(int newid){
        this.id = newid;
    }
    
    public void setFrequencia(int frequencia){
        this.freq = frequencia;
    }
    
    public void setNome(String name){
        this.nome = name;
    }

}
