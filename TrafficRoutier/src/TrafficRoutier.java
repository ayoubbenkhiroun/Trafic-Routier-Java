
import java.awt.*;
import java.applet.Applet;

class Vehicule {

    double x;
    double y;
    int lane;
    int road;
    double dx;
    double dy;
    String lbl;
    int carW;
    int carL;
    double carWaiting;
}

class CalFlow implements Runnable {

    int carnum, count;
    double carwt;
    int pauss;
    double time0, time1, timelap;



    
    @Override
    public void run() {

    }
}

class PanneauGraphique extends Applet implements Runnable {

    TrafficRoutier graph;
    int nVehicules;
    Vehicule Vehicules[] = new Vehicule[100];
    CalFlow carpermin[] = new CalFlow[5];
    Thread relaxer, flow;
    int brgflag[] = new int[5];
    double speed = 20;//vitesse véhicule
    int carwidth = 15, carlength = 30;//taille de véhicule
    int xpos[] = new int[5];
    int ypos = 300;//position de voix horizontale
    int brgright[] = new int[5];
    int brgleft[] = new int[5];
    int brgtop = ypos + carlength;
    int brgbottom = ypos - carlength;
    int rdleft[] = new int[5];
    int rdright[] = new int[5];
    int rdtop = ypos + carwidth, rdbottom = ypos - carwidth;

    PanneauGraphique(TrafficRoutier graph) {

        this.graph = graph;
//        for (int i = 0; i < 3; i++) {
             for (int i = 0; i < 3; i++) {
            carpermin[i] = new CalFlow();

            xpos[i] = 450 * (i + 1);
            brgright[i] = xpos[i] - carlength;
            brgleft[i] = xpos[i] + carlength;
            brgflag[i] = 0;
        }
        
            //tracage de chemin horizontale
            rdleft[1] = xpos[1 - 1] - carwidth;
            rdright[1] = xpos[1 - 1] + carwidth;
            //fin tracage

        rdleft[0] = 0;
        rdright[0] = 0;
    }

    int TrouverVehicule(String lbl) {
        for (int i = 0; i < nVehicules; i++) {
            if (Vehicules[i].lbl.equals(lbl)) {
                return i;
            }
        }
        return ajouterVehicule(lbl);
    }

    int ajouterVehicule(String lbl) {
        int temp;
        Vehicule n = new Vehicule();
        temp = (int) (2 * Math.random());
        if (temp == 0 || temp == 4) {
            n.x = 480 + 210 * Math.random(); //position départ véhicule
            n.y = ypos;
            n.carW = carlength;
            n.carL = carwidth;
        } else {
//            n.x = xpos[temp - 1];
            n.x = xpos[0];
            n.y = 10 + 100 * Math.random();
            n.carW = carwidth;
            n.carL = carlength;
        }
        if (temp == 2) {
            temp = 0;
        }
//        n.road = temp;
        n.road = temp;
        n.lbl = lbl;
        n.carWaiting = -1;
        Vehicules[nVehicules] = n;
        return nVehicules++;
    }

    @Override
    public void run() {

        while (true) {
            relax();
            try {
                Thread.sleep(70);//vitesse de véhicules
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    synchronized void relax() {
        for (int i = 0; i < nVehicules; i++) {
            if (Vehicules[i].road == 0) {
                Vehicules[i].dx = -speed * Math.random();
                Vehicules[i].dy = 2 * Math.random() - 1;
            } else {
                Vehicules[i].dy = speed * Math.random();
                Vehicules[i].dx = 2 * Math.random() - 1;
            }
        }
        for (int i = 0; i < nVehicules; i++) {
            Vehicule n1 = Vehicules[i];
          
            for (int j = 0; j < nVehicules; j++) {
                Vehicule n2 = Vehicules[j];
                if (i == j || n1.road != n2.road) {
                    continue;
                }
                double vx;
                if (n1.road == 0) {
                    vx = n1.x - n2.x;
                } else {
                    vx = n2.y - n1.y;
                }
                if (vx < 0) {
                    continue;
                }
                double len = vx;

                if (len < (n2.carW + n2.carL)) {
                    if (n1.carWaiting < 0) {
                        n1.carWaiting = System.currentTimeMillis();
                    }

                    if (n1.road == 0) {
                        n1.dx = 0;
                    } else {
                        n1.dy = 0;
                    }
                }
            }

        }


//mouvement véhicule
        Dimension d = size();
        double temp;
        for (int i = 0; i < nVehicules; i++) {
            Vehicule n = Vehicules[i];
            if (n.road == 0) {
                temp = n.x;
                n.x += Math.max(-10, Math.min(10, n.dx));//mouvement véhicule horizontale
                for (int k = 0; k < 3; k++) {
                    if ((n.x < brgleft[k] && n.x > brgright[k]) && brgflag[k] == 1) {
                        if (temp > brgleft[k] || temp < brgright[k]) {
                            n.x = temp;
                        }
                    } else if ((n.x < brgleft[k] && n.x > brgright[k]) && brgflag[k] == 0) {
                        brgflag[k] = 1;
                    } else if (temp < brgleft[k] && temp > brgright[k]) {
                        brgflag[k] = 0;
                    }

                    //répétition ligne horizontale des véhicules
                    if (n.x < 0) {
                        n.x = d.width - 10 * Math.random();
                        carpermin[0].carnum = carpermin[0].carnum + 1;
                    } else if (n.x > d.width) {
                        n.x = d.width - 10 * Math.random();
                    }
                    //fin répétition

                    if (n.x != temp && n.carWaiting == -1) {
                        carpermin[0].carwt += System.currentTimeMillis()
                                - n.carWaiting;
                        n.carWaiting = -1;
                    }
                }
            } else {
                temp = n.y;
                n.y += Math.max(-10, Math.min(10, n.dy));
                if ((n.y < brgtop && n.y > brgbottom) && brgflag[n.road - 1] == 1) {
                    if (temp > brgtop || temp < brgbottom) {
                        n.y = temp;
                    }
                } else if ((n.y < brgtop && n.y > brgbottom) && brgflag[n.road - 1] == 0) {
                    brgflag[n.road - 1] = 1;
                } else if (temp < brgtop && temp > brgbottom) {
                    brgflag[n.road - 1] = 0;
                }

                if (n.y > d.height || n.y < 0) {
                    n.y = 10 * Math.random();
                    carpermin[0].carnum = carpermin[0].carnum + 1;
                }
            }
        }
        //fin mouvement véhicule
        
        repaint(); //redessine tout le composant
        
    }
    Vehicule pick;
    double pickoldx, pickoldy;
    Image offscreen;
    Dimension offscreensize;
    Graphics offgraphics;
    final Color selectColor = Color.pink;
    final Color edgeColor = Color.black;

    
    //appel de fichier color.java : Color(int r, int g, int b)
    final Color VehiculeColor = new Color(255, 0, 0);

    //couleur de véhicule
    public void dessinerVehicule(Graphics g, Vehicule n) {
        int x = (int) n.x;
        int y = (int) n.y;
        
        g.setColor((n == pick) ? selectColor : VehiculeColor);
        int w = n.carW;
        int h = n.carL;
        g.fillRect(x - w / 2, y - h / 2, w, h);
        g.setColor(Color.black);
        g.drawRect(x - w / 2, y - h / 2, w - 1, h - 1);
        g.drawString("=", x - w / 2 + 2, y + h / 2 - 2);
    }


    public void DessinerRoute(Graphics g) {
        Dimension d = size();
        g.setColor(Color.gray);
        //nbr des voix verticales
        for (int k = 1; k < 2; k++) {
            g.drawLine(rdleft[k], 0, rdleft[k], rdbottom);
            g.drawLine(rdleft[k], rdtop, rdleft[k], d.height);
            g.drawLine(rdright[k], 0, rdright[k], rdbottom);
            g.drawLine(rdright[k], rdtop, rdright[k], d.height);
            g.drawLine(rdright[k - 1], rdtop, rdleft[k], rdtop);
            g.drawLine(rdright[k - 1], rdbottom, rdleft[k], rdbottom);
        }
        g.drawLine(rdright[1], rdbottom, d.width, rdbottom);//barre haut de voi horizontale
        g.drawLine(rdright[1], rdtop, d.width, rdtop);//barre bas de voi horizontale
    }
    //fin voix verticales

    @Override
    public synchronized void update(Graphics g) {

        	Dimension d = size();
	if ((offscreen == null) || (d.width != offscreensize.width) || (d.height != offscreensize.height)) {
	    offscreen = createImage(d.width, d.height);
	    offscreensize = d;
	    offgraphics = offscreen.getGraphics();
	}

	offgraphics.setColor(getBackground());
	offgraphics.fillRect(0, 0, d.width, d.height);
              DessinerRoute(offgraphics);

//draw cars
             for (int i = 0 ; i < nVehicules ; i++) {
	    dessinerVehicule(offgraphics, Vehicules[i]);
	}
	g.drawImage(offscreen, 0, 0, null);

    }

    

    @Override
    public void start() {
        relaxer = new Thread(this);
        relaxer.start();
    }

    @Override
    public void stop() {
        relaxer.stop();
    }
}

public class TrafficRoutier extends Applet {

    PanneauGraphique panel;
    int carnum;

    @Override
    public void init() {
        setLayout(new BorderLayout());

        panel = new PanneauGraphique(this);
        add("Center", panel);

        //nbr des véhicule
        carnum = 20;
        

        //dessiner les véhicules sur la route
        for (int k = 0; k < carnum; k++) {
            panel.TrouverVehicule(Integer.toString(k));
        }


        //Création de deux boutons "démarrer" et "arrêter"
        Panel btpnl = new Panel();
        add("South", btpnl);

        btpnl.add(new Button("Démarrer"));
        btpnl.add(new Button("Arrêter"));

    }

    @Override
    public boolean action(Event evt, Object arg) {
        if (((Button) evt.target).getLabel().equals("Arrêter")) {

            panel.stop();
        } else if (((Button) evt.target).getLabel().equals("Démarrer")) {
            if (!panel.relaxer.isAlive()) {
                panel.start();
            }
        }
        return true;
    }

    @Override
    public void start() {
        panel.start();
    }

    @Override
    public void stop() {
        panel.stop();
    }
}
