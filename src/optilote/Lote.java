package optilote;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

public class Lote {

    int custo;
    boolean construcao_permitida;
    Edificio building;
    
    Lote() {
        this.custo = 0;
        this.construcao_permitida = false;
        this.building = null;
    }

    Lote(int custo, boolean permissao){
        this.custo = custo;
        this.construcao_permitida = permissao;
        this.building= null;
    }
    
    Lote(Edificio build){
        this.custo = 0;
        this.construcao_permitida = false;
        this.building = build;
    }
    
    Lote(int custo, boolean permissao, Edificio build){
        this.custo = custo;
        this.construcao_permitida = permissao;
        this.building= build;
    }

    public int getCusto() {
        return this.custo;
    }

    public boolean getPermissao() {
        return this.construcao_permitida;
    }
    
    public Edificio getEdificio(){
        return building;
    }
    
    public void setCusto(int custo){
        this.custo = custo;
    }
    
    public void setPermissao(boolean permissao){
        this.construcao_permitida = permissao;
    }
    
    public void setEdificio(Edificio edi){
        this.building = edi;
    }
    

}
