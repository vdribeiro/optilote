/*
 * OptiLoteView.java
 */

package optilote;

import org.jdesktop.application.Action;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * The application's main frame.
 */
public class OptiLoteView extends FrameView implements ActionListener {
    
    org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(optilote.OptiLoteApp.class).getContext().getResourceMap(OptiLoteView.class);
    Icon empty = resourceMap.getIcon("jLabelEmpty.icon");
    Icon blocked = resourceMap.getIcon("jLabelBlocked.icon");
    Icon build = resourceMap.getIcon("jLabelFull.icon");
    Icon ps = resourceMap.getIcon("jLabelPS.icon");
    
    int newcount=5;
    int lastcount=5;
    int citymaxsize=100;
    int tamanho = 2;
    
    int tc=0;
    boolean hs=false;
    boolean hsf=false;
    
    int costs[] = new int[citymaxsize+1];
    int freqs[] = new int[citymaxsize+1];
    Lote[][] cidade;
    Algorithms calculate = new Algorithms();
    
    //public javax.swing.JLabel jLabel[] = new javax.swing.JLabel[citymaxsize+1];
    public javax.swing.JButton jLabel[] = new javax.swing.JButton[citymaxsize+1];

    public OptiLoteView(SingleFrameApplication app) {
        super(app);
        
        initComponents();
        initComps();
        
        for (int i=0;i<citymaxsize+1;i++) {
            jLabel[i].setText(null);
            //jLabel[i].setText(jLabel[i].getName());
            costs[i]=0;
            freqs[i]=0;
        }
        
        // status bar initialization - message timeout, idle icon and busy animation, etc
        //ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
        
        for (int i=5;i<citymaxsize+1;i++) {
            this.jLabel[i].setVisible(false);
        }
        
        //print resources
        /*Object[] set = resourceMap.keySet().toArray();
        for (int i=0;i<set.length;i++) {
            System.out.println(set[i].toString());
        }*/
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = OptiLoteApp.getApplication().getMainFrame();
            aboutBox = new OptiLoteAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        OptiLoteApp.getApplication().show(aboutBox);
    }
    
    @Action
    public void expandCity() {
        jMenuItem5.setEnabled(true);
        if (lastcount+newcount>citymaxsize) {
            jMenuItem1.setEnabled(false);
        }
        
        for (int i=lastcount;i<lastcount+newcount;i++) {
            jLabel[i].setVisible(true);
            jLabel[i].setIcon(empty);
            costs[i]=0;
            freqs[i]=0;
        }
        
        lastcount=lastcount+newcount;
        newcount=newcount+2;
        tamanho++;
        
        if (hs) {
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(Integer.toString(costs[i]));
            }
        }else if (hsf) {
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(Integer.toString(freqs[i]));
            }
        } else {
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(null);
            }
        }
        
        int n=Integer.parseInt(jTextField1.getText()) + 100;
        jTextField1.setText(Integer.toString(n));
        
    }
    
    @Action
    public void reduceCity() {
        jMenuItem1.setEnabled(true);
        if (lastcount<11) {
            jMenuItem5.setEnabled(false);
        }
        
        for (int i=lastcount-newcount+2;i<lastcount;i++) {
            jLabel[i].setVisible(false);
            jLabel[i].setIcon(empty);
            costs[i]=0;
            freqs[i]=0;
        }
        
        newcount=newcount-2;
        lastcount=lastcount-newcount;
        tamanho--;
        
        if (hs) {
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(Integer.toString(costs[i]));
            }
        }else if (hsf) {
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(Integer.toString(freqs[i]));
            }
        } else {
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(null);
            }
        }
        
        int n=Integer.parseInt(jTextField1.getText()) - 100;
        jTextField1.setText(Integer.toString(n));
        
    }
    
    @Action
    public void hide_showCosts() {
        hsf=false;
        if (!hs) {
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(Integer.toString(costs[i]));
            }
            hs=true;
        } else {
            hs=false;
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(null);
            }
        }
        
    }
    
    @Action
    public void hide_showFrequencies() {
        hs=false;
        if (!hsf) {
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(Integer.toString(freqs[i]));
            }
            hsf=true;
        } else {
            hsf=false;
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(null);
            }
        }
    }
    
    @Action
    public void generateNewCity() {
        for (int i=0;i<citymaxsize+1;i++) {
            jLabel[i].setText(null);
            jLabel[i].setIcon(empty);
            costs[i]=0;
            freqs[i]=0;
        }
        Random rand = new Random();
        for (int i=1;i<lastcount;i++) {
            int r = rand.nextInt(21);
            costs[i]=r;
            r = rand.nextInt(21);
            if ((r>=0) && (r<=8)) {
                jLabel[i].setIcon(empty);
            } else if ((r>=9) && (r<=16)) {
                jLabel[i].setIcon(build);
                r = rand.nextInt(21);
                freqs[i]=r;
            } else if ((r>=17) && (r<=20)) {
                jLabel[i].setIcon(blocked);
            } else {
                jLabel[i].setIcon(empty);
            }
        }
        
        if (hs) {
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(Integer.toString(costs[i]));
            }
        }else if (hsf) {
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(Integer.toString(freqs[i]));
            }
        } else {
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(null);
            }
        }
        
    }
    
    @Action
    public void calculateGenetic() {
        convertMatrix();
        int maxcost = Integer.parseInt(jTextField1.getText());
        double crossp = Double.parseDouble(jTextField2.getText());
        double mutp = Double.parseDouble(jTextField3.getText());
        int safestop = Integer.parseInt(jTextField4.getText());
        cidade=calculate.genetic(cidade,maxcost,crossp,mutp,safestop);
        drawCity();
    }
    
    @Action
    public void calculateSimulatedAnnealing() {
        convertMatrix();
        cidade=calculate.annealing(cidade);
        drawCity();
    }
    
    @Action
    public void Switch() {
        if (Switchbtn.getText().compareToIgnoreCase("Terrain")==0) {
            Switchbtn.setText("Cost");
            tc=1;
        } else if (Switchbtn.getText().compareToIgnoreCase("Cost")==0) {
            Switchbtn.setText("Frequency");
            tc=2;
        } else {
            Switchbtn.setText("Terrain");
            tc=0;
        }
        
    }
    
    @Action
    public void Reset() {
        
        for (int i=0;i<citymaxsize+1;i++) {
            if ((hs) || (hsf))
                jLabel[i].setText("0");
            else
                jLabel[i].setText(null);
            
            jLabel[i].setIcon(empty);
            costs[i]=0;
            freqs[i]=0;
        }
    }
    
    @Action
    public void noGUI() {
        //Main.menu();
        calculate.NOGUI();
    }
    
    public void convertMatrix() {
        cidade = new Lote[tamanho][tamanho];
        for (int i = 0; i < cidade.length; i++) {
            for (int j = 0; j < cidade[0].length; j++) {
                cidade[i][j]=null;
            }
        }
        //System.out.println(tamanho);
        int vari=0;
        int varj=0;
        int cont=0;
        boolean ai=true;
        for (int i=1;i<lastcount;i++) {
            Lote l = new Lote();
            l.setCusto(costs[i]);
            Icon getico = jLabel[i].getIcon();
            if (getico.equals(empty)) {
                l.setPermissao(true);
            } else if (getico.equals(blocked)) {
                l.setPermissao(false);
            } else if (getico.equals(build)) {
                l.setEdificio(new Edificio(i,freqs[i],"Edificio"));
            } else if (getico.equals(ps)) {
                l.setEdificio(new Edificio(i,freqs[i],"Hospital"));
            } else {
                //ok
            }
            
            cidade[vari][varj]= l;
            
            if (vari==varj){
                ai=false;
            }
            
            if (varj==0){
                cont++;
                vari=0;
                varj=cont;
                ai = true;
            } else {
                if (ai) {
                    vari++;
                } else {
                    varj--;
                }   
            }
        }
    }
    
    public void drawCity() {
        
        for (int i=0;i<citymaxsize+1;i++) {
            jLabel[i].setText(null);
            jLabel[i].setIcon(empty);
            costs[i]=0;
            freqs[i]=0;
        }
        
        int vari=0;
        int varj=0;
        int cont=0;
        boolean ai=true;
        for (int i=1;i<lastcount;i++) {
            Lote lot = cidade[vari][varj];
            Icon setico=null;
            
            costs[i]=lot.custo;
            if (lot.building==null) {
                if (lot.construcao_permitida) {
                    setico=empty;
                } else {
                    setico=blocked;
                }
            } else {
                if (lot.getEdificio().nome.compareToIgnoreCase("Hospital")==0) {
                    setico=ps;
                }else{
                    setico=build;
                    freqs[i]=lot.getEdificio().freq;
                }
                
            }
            
            jLabel[i].setIcon(setico);
            
            if (vari==varj){
                ai=false;
            }
            
            if (varj==0){
                cont++;
                vari=0;
                varj=cont;
                ai = true;
            } else {
                if (ai) {
                    vari++;
                } else {
                    varj--;
                }   
            }
        }
        
        if (hs) {
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(Integer.toString(costs[i]));
            }
        }else if (hsf) {
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(Integer.toString(freqs[i]));
            }
        } else {
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(null);
            }
        }
    }
    
    public void printCity() {
        String resultado = "";
        String presente = null;
        for (int i = 0; i < cidade.length; i++) {
            for (int j = 0; j < cidade[0].length; j++) {
                if (cidade[i][j].getEdificio() != null) {
                    presente = "EDIF";
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
    
    // method needed for actionlistener Interface
    public void actionPerformed(ActionEvent e){
     
        javax.swing.JButton button = (javax.swing.JButton) e.getSource();
        int btnnum=Integer.parseInt(button.getName());
        
        if (tc==2) {
            if (button.getIcon().equals(build)) {
                freqs[btnnum]++;
                if (freqs[btnnum]>20) {
                    freqs[btnnum]=0;
                }
                if (hsf)
                    button.setText(Integer.toString(freqs[btnnum]));
            }
        } else if (tc==1) {
            costs[btnnum]++;
            if (costs[btnnum]>20) {
                costs[btnnum]=0;
            }
            if (hs)
                button.setText(Integer.toString(costs[btnnum]));
        } else {
            freqs[btnnum]=0;
            button.setText(Integer.toString(freqs[btnnum]));
            if (button.getIcon().equals(empty)) {
                button.setIcon(blocked);
            } else if (button.getIcon().equals(blocked)) {
                button.setIcon(build);
            } else if (button.getIcon().equals(build)) {
                button.setIcon(empty);
            } else if (button.getIcon().equals(ps)) {
                button.setIcon(empty);
            } else {
                button.setIcon(empty);
            }
        }
        
        if (hs) {
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(Integer.toString(costs[i]));
            }
        }else if (hsf) {
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(Integer.toString(freqs[i]));
            }
        } else {
            for (int i=0;i<citymaxsize+1;i++) {
                jLabel[i].setText(null);
            }
        }
        
        //button.setText(button.getIcon().toString());
    }
    
    private void initComps() {
        //mainPanel = new javax.swing.JPanel();
        for (int i=0;i<citymaxsize+1;i++) {
            jLabel[i] = new javax.swing.JButton();
            jLabel[i].addActionListener(this);
        }

        //org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(optilote.OptiLoteApp.class).getContext().getResourceMap(OptiLoteView.class);
        for (int i=0;i<citymaxsize+1;i++) {
            jLabel[i].setIcon(empty); // NOI18N
            jLabel[i].setText("0"); // NOI18N
            jLabel[i].setName(Integer.toString(i)); // NOI18N
        }

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(jLabel[1])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[2])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[5])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[10])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[17]))
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(jLabel[4])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[3])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[6])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[11])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[18]))
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(jLabel[9])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[8])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[7])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[12])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[19])))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(jLabel[26])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[37])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[50])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[65])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[82]))
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(jLabel[27])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[38])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[51])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[66])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[83]))
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(jLabel[28])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[39])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[52])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[67])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[84]))))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel[16])
                                    .addComponent(jLabel[25]))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel[24])
                                    .addComponent(jLabel[15]))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(mainPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel[14])
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel[13]))
                                    .addGroup(mainPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel[23])
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel[22])))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel[20])
                                    .addComponent(jLabel[21])))
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel[36])
                                    .addComponent(jLabel[49])
                                    .addComponent(jLabel[64])
                                    .addComponent(jLabel[81])
                                    .addComponent(jLabel[100]))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel[35])
                                    .addComponent(jLabel[48])
                                    .addComponent(jLabel[63])
                                    .addComponent(jLabel[80])
                                    .addComponent(jLabel[99]))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel[34])
                                    .addComponent(jLabel[47])
                                    .addComponent(jLabel[62])
                                    .addComponent(jLabel[79])
                                    .addComponent(jLabel[98]))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel[33])
                                    .addComponent(jLabel[46])
                                    .addComponent(jLabel[61])
                                    .addComponent(jLabel[78])
                                    .addComponent(jLabel[97]))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel[45])
                                    .addComponent(jLabel[32])
                                    .addComponent(jLabel[60])
                                    .addComponent(jLabel[77])
                                    .addComponent(jLabel[96]))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(jLabel[29])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[40]))
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(jLabel[30])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[41]))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel[31])
                                    .addComponent(jLabel[44])
                                    .addComponent(jLabel[59])
                                    .addComponent(jLabel[76])
                                    .addComponent(jLabel[95]))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel[43])
                                    .addComponent(jLabel[42])
                                    .addComponent(jLabel[58])
                                    .addComponent(jLabel[75])
                                    .addComponent(jLabel[94]))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(jLabel[56])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[71])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[88]))
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(jLabel[55])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[70])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[87]))
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(jLabel[54])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[69])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[86]))
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(jLabel[53])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[68])
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel[85]))
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel[57])
                                    .addComponent(jLabel[74])
                                    .addComponent(jLabel[93]))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(mainPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel[72])
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel[89]))
                                    .addGroup(mainPanelLayout.createSequentialGroup()
                                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel[73])
                                            .addComponent(jLabel[92]))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel[91])
                                            .addComponent(jLabel[90]))))))))
                .addGap(18, 18, 18)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(570, Short.MAX_VALUE))
        );
        
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

                    .addComponent(jLabel[2])
                    .addComponent(jLabel[1])
                    .addComponent(jLabel[5])
                    .addComponent(jLabel[10])
                    .addComponent(jLabel[17])
                    .addComponent(jLabel[26])
                    .addComponent(jLabel[37])
                    .addComponent(jLabel[50])
                    .addComponent(jLabel[65])
                    .addComponent(jLabel[82]))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel[4])
                    .addComponent(jLabel[3])
                    .addComponent(jLabel[18])
                    .addComponent(jLabel[6])
                    .addComponent(jLabel[11])
                    .addComponent(jLabel[27])
                    .addComponent(jLabel[38])
                    .addComponent(jLabel[51])
                    .addComponent(jLabel[66])
                    .addComponent(jLabel[83]))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel[19])
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel[8], javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel[9], javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel[7], javax.swing.GroupLayout.Alignment.TRAILING))
                    .addComponent(jLabel[12])
                    .addComponent(jLabel[28])
                    .addComponent(jLabel[39])
                    .addComponent(jLabel[52])
                    .addComponent(jLabel[67])
                    .addComponent(jLabel[84]))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel[13], javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel[14], javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel[15], javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel[20], javax.swing.GroupLayout.Alignment.TRAILING))
                        .addComponent(jLabel[16])
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel[40])
                            .addComponent(jLabel[29])))
                    .addComponent(jLabel[53])
                    .addComponent(jLabel[68])
                    .addComponent(jLabel[85]))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel[30])
                    .addComponent(jLabel[25])
                    .addComponent(jLabel[23])
                    .addComponent(jLabel[21])
                    .addComponent(jLabel[22])
                    .addComponent(jLabel[24])
                    .addComponent(jLabel[41])
                    .addComponent(jLabel[54])
                    .addComponent(jLabel[69])
                    .addComponent(jLabel[86]))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel[42])
                                    .addComponent(jLabel[32])
                                    .addComponent(jLabel[31])
                                    .addComponent(jLabel[33])
                                    .addComponent(jLabel[34])
                                    .addComponent(jLabel[35])
                                    .addComponent(jLabel[36]))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel[43])
                                    .addComponent(jLabel[44])
                                    .addComponent(jLabel[45])
                                    .addComponent(jLabel[46])
                                    .addComponent(jLabel[47])
                                    .addComponent(jLabel[48])
                                    .addComponent(jLabel[49])))
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel[55])
                                    .addComponent(jLabel[70]))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel[71])
                                    .addComponent(jLabel[56])
                                    .addComponent(jLabel[88]))))
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel[57])
                                    .addComponent(jLabel[58])
                                    .addComponent(jLabel[59])
                                    .addComponent(jLabel[60])
                                    .addComponent(jLabel[61])
                                    .addComponent(jLabel[62])
                                    .addComponent(jLabel[63])
                                    .addComponent(jLabel[64])))
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel[89])
                                    .addComponent(jLabel[72])))))
                    .addComponent(jLabel[87]))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel[73])
                        .addComponent(jLabel[74])
                        .addComponent(jLabel[75])
                        .addComponent(jLabel[76])
                        .addComponent(jLabel[77])
                        .addComponent(jLabel[78])
                        .addComponent(jLabel[79])
                        .addComponent(jLabel[80])
                        .addComponent(jLabel[81]))
                    .addComponent(jLabel[90]))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel[91])
                    .addComponent(jLabel[92])
                    .addComponent(jLabel[93])
                    .addComponent(jLabel[94])
                    .addComponent(jLabel[95])
                    .addComponent(jLabel[96])
                    .addComponent(jLabel[97])
                    .addComponent(jLabel[98])
                    .addComponent(jLabel[99])
                    .addComponent(jLabel[100]))))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        setComponent(mainPanel);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabelEmpty = new javax.swing.JLabel();
        jLabelBlocked = new javax.swing.JLabel();
        jLabelFull = new javax.swing.JLabel();
        jLabelPS = new javax.swing.JLabel();
        Switchbtn = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        Resetbtn = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setMaximumSize(new java.awt.Dimension(1600, 1024));
        mainPanel.setMinimumSize(new java.awt.Dimension(640, 480));
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new java.awt.Dimension(1024, 768));

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(optilote.OptiLoteApp.class).getContext().getResourceMap(OptiLoteView.class);
        jLabelEmpty.setIcon(resourceMap.getIcon("jLabelEmpty.icon")); // NOI18N
        jLabelEmpty.setText(resourceMap.getString("jLabelEmpty.text")); // NOI18N
        jLabelEmpty.setName("jLabelEmpty"); // NOI18N

        jLabelBlocked.setIcon(resourceMap.getIcon("jLabelBlocked.icon")); // NOI18N
        jLabelBlocked.setText(resourceMap.getString("jLabelBlocked.text")); // NOI18N
        jLabelBlocked.setName("jLabelBlocked"); // NOI18N

        jLabelFull.setIcon(resourceMap.getIcon("jLabelFull.icon")); // NOI18N
        jLabelFull.setText(resourceMap.getString("jLabelFull.text")); // NOI18N
        jLabelFull.setName("jLabelFull"); // NOI18N

        jLabelPS.setIcon(resourceMap.getIcon("jLabelPS.icon")); // NOI18N
        jLabelPS.setText(resourceMap.getString("jLabelPS.text")); // NOI18N
        jLabelPS.setName("jLabelPS"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(optilote.OptiLoteApp.class).getContext().getActionMap(OptiLoteView.class, this);
        Switchbtn.setAction(actionMap.get("Switch")); // NOI18N
        Switchbtn.setFont(resourceMap.getFont("Switchbtn.font")); // NOI18N
        Switchbtn.setText(resourceMap.getString("Switchbtn.text")); // NOI18N
        Switchbtn.setName("Switchbtn"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        Resetbtn.setAction(actionMap.get("Reset")); // NOI18N
        Resetbtn.setFont(resourceMap.getFont("Resetbtn.font")); // NOI18N
        Resetbtn.setLabel(resourceMap.getString("Resetbtn.label")); // NOI18N
        Resetbtn.setName("Resetbtn"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jTextField1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField1.setText(resourceMap.getString("jTextField1.text")); // NOI18N
        jTextField1.setName("jTextField1"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jTextField2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField2.setText(resourceMap.getString("jTextField2.text")); // NOI18N
        jTextField2.setName("jTextField2"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jTextField3.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField3.setText(resourceMap.getString("jTextField3.text")); // NOI18N
        jTextField3.setName("jTextField3"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jTextField4.setText(resourceMap.getString("jTextField4.text")); // NOI18N
        jTextField4.setName("jTextField4"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 78, Short.MAX_VALUE)
                        .addComponent(jLabel5))
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelFull)
                            .addComponent(jLabelEmpty))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelBlocked)
                            .addComponent(jLabelPS)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 100, Short.MAX_VALUE)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel2)
                                .addComponent(Resetbtn))
                            .addGap(37, 37, 37)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel4)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel3)))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(Switchbtn, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(112, 112, 112))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelEmpty)
                    .addComponent(jLabelBlocked))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelFull)
                    .addComponent(jLabelPS))
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Switchbtn, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(Resetbtn, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel6)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(37, Short.MAX_VALUE))
        );

        jLabelEmpty.getAccessibleContext().setAccessibleName(resourceMap.getString("jLabel101.AccessibleContext.accessibleName")); // NOI18N
        jLabelBlocked.getAccessibleContext().setAccessibleName(resourceMap.getString("jLabel102.AccessibleContext.accessibleName")); // NOI18N
        jLabelFull.getAccessibleContext().setAccessibleName(resourceMap.getString("jLabel103.AccessibleContext.accessibleName")); // NOI18N
        jLabelPS.getAccessibleContext().setAccessibleName(resourceMap.getString("jLabel104.AccessibleContext.accessibleName")); // NOI18N
        Resetbtn.getAccessibleContext().setAccessibleName(resourceMap.getString("Resetbtn.AccessibleContext.accessibleName")); // NOI18N

        jTabbedPane1.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(577, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 416, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(156, Short.MAX_VALUE))
        );

        menuBar.setMaximumSize(new java.awt.Dimension(1600, 1024));
        menuBar.setName("menuBar"); // NOI18N
        menuBar.setPreferredSize(new java.awt.Dimension(640, 21));

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        jMenu2.setIcon(resourceMap.getIcon("jMenu2.icon")); // NOI18N
        jMenu2.setText(resourceMap.getString("jMenu2.text")); // NOI18N
        jMenu2.setName("jMenu2"); // NOI18N

        jMenuItem1.setAction(actionMap.get("expandCity")); // NOI18N
        jMenuItem1.setIcon(resourceMap.getIcon("jMenuItem1.icon")); // NOI18N
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jMenu2.add(jMenuItem1);

        jMenuItem5.setAction(actionMap.get("reduceCity")); // NOI18N
        jMenuItem5.setIcon(resourceMap.getIcon("jMenuItem5.icon")); // NOI18N
        jMenuItem5.setText(resourceMap.getString("jMenuItem5.text")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        jMenu2.add(jMenuItem5);

        fileMenu.add(jMenu2);

        jMenuItem3.setAction(actionMap.get("generateNewCity")); // NOI18N
        jMenuItem3.setIcon(resourceMap.getIcon("jMenuItem3.icon")); // NOI18N
        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        fileMenu.add(jMenuItem3);

        jMenuItem2.setAction(actionMap.get("hide_showCosts")); // NOI18N
        jMenuItem2.setIcon(resourceMap.getIcon("jMenuItem2.icon")); // NOI18N
        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        fileMenu.add(jMenuItem2);

        jMenuItem6.setAction(actionMap.get("hide_showFrequencies")); // NOI18N
        jMenuItem6.setIcon(resourceMap.getIcon("jMenuItem6.icon")); // NOI18N
        jMenuItem6.setText(resourceMap.getString("jMenuItem6.text")); // NOI18N
        jMenuItem6.setName("jMenuItem6"); // NOI18N
        fileMenu.add(jMenuItem6);

        jMenu1.setIcon(resourceMap.getIcon("jMenu1.icon")); // NOI18N
        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenuItem4.setAction(actionMap.get("calculateGenetic")); // NOI18N
        jMenuItem4.setIcon(resourceMap.getIcon("jMenuItem4.icon")); // NOI18N
        jMenuItem4.setText(resourceMap.getString("jMenuItem4.text")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        jMenu1.add(jMenuItem4);

        jMenuItem7.setAction(actionMap.get("calculateSimulatedAnnealing")); // NOI18N
        jMenuItem7.setIcon(resourceMap.getIcon("jMenuItem7.icon")); // NOI18N
        jMenuItem7.setText(resourceMap.getString("jMenuItem7.text")); // NOI18N
        jMenuItem7.setName("jMenuItem7"); // NOI18N
        jMenu1.add(jMenuItem7);

        fileMenu.add(jMenu1);

        jMenuItem8.setAction(actionMap.get("noGUI")); // NOI18N
        jMenuItem8.setIcon(resourceMap.getIcon("jMenuItem8.icon")); // NOI18N
        jMenuItem8.setText(resourceMap.getString("jMenuItem8.text")); // NOI18N
        jMenuItem8.setName("jMenuItem8"); // NOI18N
        fileMenu.add(jMenuItem8);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setIcon(resourceMap.getIcon("exitMenuItem.icon")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setIcon(resourceMap.getIcon("aboutMenuItem.icon")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setMaximumSize(new java.awt.Dimension(1600, 1024));
        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 841, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 671, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Resetbtn;
    private javax.swing.JButton Switchbtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabelBlocked;
    private javax.swing.JLabel jLabelEmpty;
    private javax.swing.JLabel jLabelFull;
    private javax.swing.JLabel jLabelPS;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
