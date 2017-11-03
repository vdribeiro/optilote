package optilote;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JOptionPane;


public class Algorithms{
    
    ArrayList<Integer> cromossoma;
    ArrayList<String> equivalencias;
    
    Algorithms() {
        cromossoma = new ArrayList<Integer>();
        equivalencias = new ArrayList<String>();
    }
    
    public static long factorial(int n) {
        int i;
        long f = 1;
        for (i = 2; i <= n; i++) {
            f = f * i;
        }
        return f;
    }

    public static long combin(int n, int k) {
        return factorial(n) / (factorial(k) * factorial(n - k));
    }
    
    public static double roundSixDecimals(double d) {
        DecimalFormat sevenDForm = new DecimalFormat("#.#######");
        DecimalFormat sixDForm = new DecimalFormat("#.######");
        double seteCasas = Double.valueOf(sevenDForm.format(d));
        double seisCasas = Double.valueOf(sixDForm.format(d));
        String aux = ""+seteCasas;
        int last_int = 0;
        if(aux.length()==9){
            char last = aux.charAt(aux.length()-1);
            last_int = Integer.parseInt(String.valueOf(last));
            if(last_int>4){
                seisCasas = Double.valueOf(sixDForm.format(d));
                seisCasas = seisCasas+ 0.000001;
            }
        }
        return seisCasas;
    }
    
    public int getEmptyLots(Lote[][] cidade) {
        int count=0;
        for (int i = 0; i < cidade.length; i++) {
            for (int j = 0; j < cidade[i].length; j++) {
                if (cidade[i][j].getEdificio()==null) {
                    count++;
                }
            }
        }
        return count;
    }
    
    public int getAvailableLots(Lote[][] cidade) {
        int count=0;
        for (int i = 0; i < cidade.length; i++) {
            for (int j = 0; j < cidade[i].length; j++) {
                if (cidade[i][j].getPermissao()) {
                    count++;
                }
            }
        }
        return count;
    }
    
    public void printCity(Lote[][] cidade) {
        String resultado = "";
        String presente = null;
        for (int i = 0; i < cidade.length; i++) {
            for (int j = 0; j < cidade[0].length; j++) {
                if (cidade[i][j].getEdificio() != null) {
                    if (cidade[i][j].getEdificio().getNome().compareToIgnoreCase("Hospital")==0) {
                        presente = "HOSP";
                    } else {
                        presente = "EDIF";
                    }
                } else {
                    presente = "null";
                }
                if (j == 0) {
                    resultado += "[" + presente;
                } else {
                    resultado += "," + presente;
                }
                if (j == (cidade[0].length - 1)) {
                    resultado += "]\n";
                }
            }
        }
        System.out.println(resultado);
        resultado = "";
        for (int i = 0; i < cidade.length; i++) {
            for (int j = 0; j < cidade[0].length; j++) {
                if (cidade[i][j].getPermissao() != false) {
                    presente = "true";
                } else {
                    presente = "false";
                }
                if (j == 0) {
                    resultado += "[" + presente;
                } else {
                    resultado += "," + presente;
                }
                if (j == (cidade[0].length - 1)) {
                    resultado += "]\n";
                }
            }
        }
        System.out.println(resultado);
    }
    //----------------------------------------------------------------------------
    
    //Genetic
    public void buildInitialChromosome(Lote[][] cidade) {
        for (int i = 0; i < cidade.length; i++) {
            for (int j = 0; j < cidade[i].length; j++) {
                if (cidade[i][j].getPermissao()) {
                    cromossoma.add(0);
                    equivalencias.add(i+"."+j);
                }
            }
        }
    }
    
    public ArrayList<ArrayList<Integer>> generateFirstSolutions(int solucoes, int lotes_vazios) {
        Random rand = new Random();
        ArrayList<ArrayList<Integer>> cromossomas_solucoes = new ArrayList<ArrayList<Integer>>();
        int prontos_socorro=0,pos=-1;
       
        //gerar pontos soccoro em posicoes aleatorias sem repetir a mesma posicao aleatoria
        ArrayList<Integer> posicoes = new ArrayList<Integer>();
       
        for (int i = 0; i < solucoes; i++) {
            //todos os cromossomas sao iniciados com os genes a 0
            ArrayList<Integer> ini_cromossoma = new ArrayList<Integer>();
            for (int j = 0; j < cromossoma.size(); j++) {
                ini_cromossoma.add(0);
            }
            cromossomas_solucoes.add(ini_cromossoma);
            
            //inicia posicoes
            posicoes.clear();
            for (int o = 0; o < cromossoma.size(); o++) {
                posicoes.add(o);
            }
            
            //criar cidades com numero de prontos de socorro random
            prontos_socorro = 1+rand.nextInt(lotes_vazios);
            int valor_na_pos;
            //colocar os prontos-socorro nas posicoes random sem repetir a mesma posicao random
            for (int j = 0; j < prontos_socorro; j++) {
                pos = rand.nextInt(posicoes.size());
                valor_na_pos = posicoes.get(pos);
                cromossomas_solucoes.get(i).set(valor_na_pos, 1);
                posicoes.remove(pos);
            }
            System.out.println("cromossoma "+cromossomas_solucoes.get(i).toString());
        }
        
        return cromossomas_solucoes;
    }
    
    private int searchNearestHospital(int lin, int col, ArrayList<String> possoc) {
        
        //print parameters
        /*System.out.println("Edificio: " + lin + "." + col);
        System.out.println("Hospitais:");
        for(int v=0;v<possoc.size();v++) {
            System.out.print(possoc.get(v) + " ");
        }
        System.out.println();*/
        
        int cycle=1;
        String ponto=null;
        String p=null;
        while (ponto==null) {
            //procura em forma circular
            
            //em cima da esquerda para direita
            if (ponto==null) {
                for (int u=col-cycle;u<=col+cycle;u++) {
                    p = Integer.toString(lin-cycle) + "." + Integer.toString(u);
                    //System.out.println("Ponto1: " + p);
                    if (possoc.contains(p)) {
                        ponto = p;
                        break;
                    }
                }
            }
            
            //na direira de cima para baixo
            if (ponto==null) {
                for (int u=lin-cycle;u<=lin+cycle;u++) {
                    p = Integer.toString(u) + "." + Integer.toString(col+cycle);
                    //System.out.println("Ponto2: " + p);
                    if (possoc.contains(p)) {
                        ponto = p;
                        break;
                    }
                }
            }
            
            //em baixo da direita para a esquerda
            if (ponto==null) {
                for (int u=col+cycle;u>=col-cycle;u--) {
                    p = Integer.toString(lin+cycle) + "." + Integer.toString(u);
                    //System.out.println("Ponto3: " + p);
                    if (possoc.contains(p)) {
                        ponto = p;
                        break;
                    }
                }
            }
            
            //na esquerda de baixo para cima
            if (ponto==null) {
                for (int u=lin+cycle;u>=lin-cycle;u--) {
                    p = Integer.toString(u) + "." + Integer.toString(col-cycle);
                    //System.out.println("Ponto4: " + p);
                    if (possoc.contains(p)) {
                        ponto = p;
                        break;
                    }
                }
            }
            
            cycle++;
            if (cycle==100) break;
        }
        
        if (ponto==null) {
            return 0;
        }
        
        int dist=0;
        int ex=lin;
        int ey=col;
        //String[] s = ponto.split(".");
        //int hx=Integer.parseInt(s[0]);
        //int hy=Integer.parseInt(s[1]);
        
        int hx=Integer.parseInt(ponto.substring(0, ponto.indexOf(".")));
        int hy=Integer.parseInt(ponto.substring(ponto.indexOf(".")+1,ponto.length()));
        
        while (ex!=hx) {
            if (ex<hx) {
                ex++;
            } else {
                ex--;
            }
            dist++;
            if (dist>100) break;
        }
        
        while (ey!=hy) {
            if (ey<hy) {
                ey++;
            } else {
                ey--;
            }
            dist++;
            if (dist>100) break;
        }
        
        System.out.println("Edificio: "+ lin + "." + col + 
                " com Hospital: " + ponto + " a distancia: " + dist);
        
        return dist;
        
    }
    
    public ArrayList<Integer> solutionsCost(Lote[][] cidade, ArrayList<ArrayList<Integer>> cromossomas_solucoes) {
        //calcular custo de cada solucao
        ArrayList<Integer> custos=new ArrayList<Integer>();
        //percorro todas as solucoes
        for (int k = 0; k < cromossomas_solucoes.size(); k++) {
            int totaldist=0;
            int totalhosp=0;
            int totalcost=0;
            
            //para cada solucao elaboro uma lista de posicoes que tem prontos socorro
            ArrayList<String> possoc = new ArrayList<String>();
            
            //percorro o cromossoma e obtenho custos dos lotes
            ArrayList<Integer> cromo = new ArrayList<Integer>(cromossomas_solucoes.get(k));
            System.out.println("Solucao " + k + "\n");
            for (int l=0; l <cromo.size();l++) {
                int pc = cromo.get(l);
                if (pc==1) {
                    String eq = equivalencias.get(l);
                    int hx=Integer.parseInt(eq.substring(0, eq.indexOf(".")));
                    int hy=Integer.parseInt(eq.substring(eq.indexOf(".")+1,eq.length()));
                    //System.out.println("Pontos: " + hx + "." + hy);
                    totalhosp=totalhosp+cidade[hx][hy].getCusto();
                    possoc.add(eq);
                    System.out.println("Hospital: " + equivalencias.get(l) + " com custo " + cidade[hx][hy].getCusto());
                }
            }
            System.out.println("Custo de Hospitais: "+ totalhosp +" \n");
            
            //percorro a cidade e obtenho as distancias
            for (int i = 0; i < cidade.length; i++) {
                for (int j = 0; j < cidade[i].length; j++) {
                    if (cidade[i][j].getEdificio()!=null) {
                        int freq = cidade[i][j].getEdificio().getFrequencia();
                        int dist = searchNearestHospital(i, j, possoc);
                        totaldist=totaldist + (freq*dist);
                    }
                }
            }
            System.out.println("Custo de Distancias: "+ totaldist +" \n");
            
            totalcost=totalhosp+totaldist;
            
            System.out.println("Custo Total da solucao: "+ totalcost +" \n");
            custos.add(totalcost);
        }
        
        return custos;
    }
    
    private ArrayList<ArrayList<Integer>> evolveSolutions(ArrayList<ArrayList<Integer>> cromossomas_solucoes, ArrayList<Double> probs, double Pcruzamento, double Pmutacao) {
        
        Random rand = new Random();
        
        //fazer roleta, gerar aleatorios para escolher quais as solucoes da roleta que vao ficar
        ArrayList<Double> roleta = new ArrayList<Double>();
        
        double somatorio = 0;
        for (int i = 0; i < probs.size()-1; i++) {
            somatorio += probs.get(i);
            //roleta.add(roundSixDecimals(somatorio));
            roleta.add(somatorio);
        }
        somatorio=1.0;
        roleta.add(somatorio);
        System.out.println("\nRoleta: " + roleta.toString() + "\n");
        
        //gera probabilidades aleatorias de solucoes aleatorias
        double rand_prob = 0;
        ArrayList<ArrayList<Integer>> cromossomas_seleccionados = new ArrayList<ArrayList<Integer>>();
        ArrayList<Double> probabilidades_seleccionados = new ArrayList<Double>();
        for (int j = 0; j < cromossomas_solucoes.size(); j++) {
            rand_prob = rand.nextDouble();
            for (int i = 0; i < roleta.size(); i++) {
                if(rand_prob<=roleta.get(i)){
                    cromossomas_seleccionados.add(new ArrayList<Integer>(cromossomas_solucoes.get(i)));
                    probabilidades_seleccionados.add(probs.get(i));
                    break;
                }
            }
        }
        
        //System.out.println("Selecionados: "+probabilidades_seleccionados.toString());
        for(int i = 0;i<cromossomas_seleccionados.size();i++){
            System.out.println("Selecionado "+i+" = " + cromossomas_seleccionados.get(i) + " -> prob = "+probabilidades_seleccionados.get(i));
        }

        //cross over
        ArrayList<ArrayList<Integer>> crom_to_cross = new ArrayList<ArrayList<Integer>>();
        //ArrayList<Integer> help = new ArrayList<Integer>();
        ArrayList<Integer> pos_crom_to_cross = new ArrayList<Integer>();
        //encontra os cromossomas com probabilidade inferior à Pcruzamento indicada
        for (int i = 0; i < probabilidades_seleccionados.size(); i++) {
            if(probabilidades_seleccionados.get(i) < Pcruzamento){
                //help = (ArrayList<Integer>)cromossomas_seleccionados.get(i).clone();
                //crom_to_cross.add(help);
                crom_to_cross.add(new ArrayList<Integer>(cromossomas_seleccionados.get(i)));
                pos_crom_to_cross.add(i);
            }
        }
   
        //cruza os cromossomas
        //para cada par de cromossomas criar ponto de cross-over
        int ponto_cross_over = 0;
        //se numero par de cromossomas seleccionados fazer normal (for avanca de 2 em 2 porque são cruzados 2 a 2
        ArrayList<Integer> auxiliar1 = new ArrayList<Integer>();
        ArrayList<Integer> auxiliar2 = new ArrayList<Integer>();
        ArrayList<Integer> pont = new ArrayList<Integer>();     

        System.out.println("Antes = "+crom_to_cross.toString());
        if (crom_to_cross.size()>1) {
            
            // CASO TAMANHO CROM_TO_CROSS SEJA IMPAR
            if((crom_to_cross.size()%2) != 0){// E SE SIZE!= 1
                ponto_cross_over = 1+rand.nextInt((crom_to_cross.get(0).size()-1));
                for (int j = 0; j < ponto_cross_over; j++) {
                    auxiliar2.add(crom_to_cross.get(crom_to_cross.size()-2).get(j));
                    //substituir informação
                    crom_to_cross.get(crom_to_cross.size()-1).set(j, auxiliar2.get(j));
                }
            } //else {
            if(crom_to_cross.size()>1){
                for (int i = 0; i < (crom_to_cross.size()-1); i+=2) {
                    ponto_cross_over = 1+rand.nextInt((crom_to_cross.get(0).size()-1));
                    pont.add(ponto_cross_over); //meramente teste
                    auxiliar1.clear();
                    auxiliar2.clear();
                    //percorre cromossoma até ao ponto de cross
                    for (int j = 0; j < ponto_cross_over; j++) {
                        //copiar informacao dos 2 cromossomas para array auxiliar
                        auxiliar1.add(crom_to_cross.get(i).get(j));
                        auxiliar2.add(crom_to_cross.get(i+1).get(j));
                        //substituir informação
                        crom_to_cross.get(i).set(j, auxiliar2.get(j));
                        crom_to_cross.get(i+1).set(j, auxiliar1.get(j));
                    }
                }

                System.out.println("pontos de corte = "+pont.toString());
                System.out.println("depois = "+crom_to_cross.toString()+"\n");

                for (int i = 0; i < crom_to_cross.size(); i++) {
                    cromossomas_seleccionados.set(pos_crom_to_cross.get(i),crom_to_cross.get(i));
                }

                System.out.println("nums crossed = " + pos_crom_to_cross.toString());
                System.out.println("Cromossomas seleccionados ja com cross_over :\n");
                for(int i = 0;i<cromossomas_seleccionados.size();i++){
                    System.out.println("crom "+i+" = " + cromossomas_seleccionados.get(i));
                }
            }
        }
        
        // mutacao
        double rand_mutacao;
        for(int i = 0;i<cromossomas_seleccionados.size();i++){
            for(int j = 0;j< cromossomas_seleccionados.get(i).size();j++){
                rand_mutacao = rand.nextDouble();
                if(rand_mutacao < Pmutacao){
                    System.out.println("Mutação no bit "+ ((i*cromossomas_seleccionados.size())+j));
                    System.out.println("Cromossoma "+i+" e bit "+(j+1));
                    if(cromossomas_seleccionados.get(i).get(j) == 0)
                        cromossomas_seleccionados.get(i).set(j,1);
                    else
                        if(cromossomas_seleccionados.get(i).get(j) == 1)
                            cromossomas_seleccionados.get(i).set(j,0);
                }
            }
        }

        for(int i = 0;i<cromossomas_seleccionados.size();i++){
            System.out.println("crom "+i+" = " + cromossomas_seleccionados.get(i));
        }
        
        return cromossomas_seleccionados;
    }
    
    public Lote[][] genetic(Lote[][] cidade, int expected, double pcruza, double pmut, int safestop) {
        boolean first=true;
        cromossoma = new ArrayList<Integer>();
        equivalencias = new ArrayList<String>();
        
        //numero de lotes vazios na cidade
        int lotes_vazios=getAvailableLots(cidade);
        //constroi o cromossoma da cidade
        buildInitialChromosome(cidade);
        
        //lista de cromossomas solucao
        ArrayList<ArrayList<Integer>> cromossomas_solucoes = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> solucao = new ArrayList<Integer>();
        //e seus custos e probabilidades associados
        ArrayList<Integer> costs = new ArrayList<Integer>();
        ArrayList<Double> probs = new ArrayList<Double>();
        
        //numero de solucoes a gerar
        int solucoes = 20;
        //se existirem poucos lotes disponiveis
        //o numero maximo de solucoes nao pode ser 20
        //calcula-se entao o numero maximo de solucoes
        //atraves de combinacoes
        if(lotes_vazios < 5 ){
            solucoes = 0;
            for (int i = 0; i < lotes_vazios; i++) {
                solucoes += combin(lotes_vazios,i);
            }
        }
        
        // imprime operacao
        System.out.println("Cromossoma da Cidade: \n"+cromossoma.toString());
        System.out.println("Equivalencias: \n"+equivalencias.toString()+"\n");
        System.out.println("solucoes = "+solucoes);
        
        //procura solucao
        int best=Integer.MAX_VALUE;
        int niteracoes=0;
        do {
            niteracoes++;
            System.out.println("Iteracao: " + niteracoes );
            
            if (first) {
                first=false;
                //lista de cromossomas solucoes iniciais
                cromossomas_solucoes = new ArrayList<ArrayList<Integer>> (generateFirstSolutions(solucoes,lotes_vazios));
                
            } else {
                //gera novas evolucoes
                cromossomas_solucoes = new ArrayList<ArrayList<Integer>> (evolveSolutions(cromossomas_solucoes,probs, pcruza, pmut));
            }
            System.out.println();

            //calcula o custo de cada solucao
            costs.clear();
            costs=new ArrayList<Integer>(solutionsCost(cidade,cromossomas_solucoes));
            //obtem custo total das solucoes
            double soma=0;
            for (int i=0;i<costs.size();i++) {
                soma=soma+costs.get(i);
            }
            System.out.println("Custo Total das Solucoes: " + soma);
            //obtem as probabilidades de cada solucao
            //e como queremos minimizar guarda o mais baixo
            double temp=Double.MAX_VALUE;
            int posc=0;
            probs = new ArrayList<Double>();
            for (int i=0;i<costs.size();i++) {
                double c = costs.get(i);
                double prob = c/soma;
                if (prob<temp) {
                    temp=prob;
                    posc=i;
                }
                probs.add(prob);
                System.out.println("Probabilidade " + i + ": " + prob + " com custo " +  costs.get(i));
            }
            best=costs.get(posc);
            solucao=cromossomas_solucoes.get(posc);
            System.out.println("Melhor probabilidade: " + temp + 
                    " da solucao " +  posc + " com custo " + best);
            
            //cromossomas_solucoes=evolveSolutions(cromossomas_solucoes,probs,pcruza);
            
            //se o custo for zero nao e valido
            if (best==0) {
                System.out.println("Custo nao pode ser zero");
                best=Integer.MAX_VALUE;
            }
            
        } while ((best>expected) && (niteracoes<safestop));
        
        System.out.println("\nNumero de iteracoes: " + niteracoes );
        System.out.println("Melhor Custo: " + best );
        
        JOptionPane.showMessageDialog(null, "Melhor Custo: " + best);
        
        //actualiza a cidade
        for (int i=0;i<solucao.size();i++) {
            if (solucao.get(i)==1) {
                String eq = equivalencias.get(i);
                int hx=Integer.parseInt(eq.substring(0, eq.indexOf(".")));
                int hy=Integer.parseInt(eq.substring(eq.indexOf(".")+1,eq.length()));
                cidade[hx][hy].setEdificio(new Edificio(i,0,"Hospital"));
            }
        }
        
        return cidade;
    }
   
    public void NOGUI() {
        Lote[][] cidade;
        int buildings_id = 0;
        Random rand = new Random();
        int custo_lote=0;
        boolean permissao=false;

        String tamanho_cidade = JOptionPane.showInputDialog(null, "Introduza o tamanho da cidade ( X,Y ):");
        String num_edificios = JOptionPane.showInputDialog(null, "Quantos edificios já contém a cidade?");
        int maxcost = Integer.parseInt(JOptionPane.showInputDialog(null, "Qual é o custo maximo esperado?"));
        double crossp = Double.parseDouble(JOptionPane.showInputDialog(null, "Qual é a probabilidade de cruzamento?"));
        double mutp = Double.parseDouble(JOptionPane.showInputDialog(null, "Qual é a probabilidade de mutação?"));
        int safestop = Integer.parseInt(JOptionPane.showInputDialog(null, "Qual é o numero maximo de iterações?"));
        
        //cidade=genetic(cidade,maxcost,crossp,mutp,safestop);
        
        String[] array_cidade = tamanho_cidade.split(",");
        cidade = new Lote[Integer.parseInt(array_cidade[0])][Integer.parseInt(array_cidade[1])];

        for (int i = 0; i < cidade.length; i++) {
            for (int j = 0; j < cidade[0].length; j++) {
                custo_lote = rand.nextInt(21);
                permissao = rand.nextBoolean();
                cidade[i][j] = new Lote(custo_lote, permissao);
            }
        }
        
        int linhas = 0, colunas = 0,index = -1;
        for (int i = 0; i < Integer.parseInt(num_edificios); i++) {
            linhas = rand.nextInt(Integer.parseInt(array_cidade[0]));
            colunas = rand.nextInt(Integer.parseInt(array_cidade[1]));
            if (cidade[linhas][colunas].getEdificio() == null) {
                int freq = rand.nextInt(21);
                Edificio building = new Edificio(buildings_id, freq, "Edificio");
                buildings_id++;
                cidade[linhas][colunas].setEdificio(building);
                cidade[linhas][colunas].setPermissao(false);
            } else {
                i--;
            }
        }

        System.out.println("\nCidade antes: ");
        printCity(cidade);
        
        cidade=genetic(cidade,maxcost,crossp,mutp,safestop);
        
        System.out.println("\nCidade depois: ");
        printCity(cidade);
    }
    
    //----------------------------------------------------------------------------

    //Simulated Annealing
    public Lote[][] annealing(Lote[][] cidade) {
        return cidade;
    }
    //----------------------------------------------------------------------------

}
