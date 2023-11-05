import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;

public class ExpeAvance {
	
	private static String afficheTemps(int secondes) {
		int m=0, h=0, s=secondes;
		while(s>=60) {
			m++; s-=60;
			while(m>=60) {
				h++; m-=60;
			}
		}
		if(h>0) {return h+"H"+m+"min";}
		else {return m+"min";}
		
	}

	private static Model lireReseau(BufferedReader in) throws Exception{
		Model model = new Model("Expe");
		int nbVariables = Integer.parseInt(in.readLine());				// le nombre de variables
		int tailleDom = Integer.parseInt(in.readLine());				// la valeur max des domaines
		IntVar []var = model.intVarArray("x",nbVariables,0,tailleDom-1); 	
		int nbConstraints = Integer.parseInt(in.readLine());			// le nombre de contraintes binaires		
		for(int k=1;k<=nbConstraints;k++) { 
			String chaine[] = in.readLine().split(";");
			IntVar portee[] = new IntVar[]{var[Integer.parseInt(chaine[0])],var[Integer.parseInt(chaine[1])]}; 
			int nbTuples = Integer.parseInt(in.readLine());				// le nombre de tuples		
			Tuples tuples = new Tuples(new int[][]{},true);
			for(int nb=1;nb<=nbTuples;nb++) { 
				chaine = in.readLine().split(";");
				int t[] = new int[]{Integer.parseInt(chaine[0]), Integer.parseInt(chaine[1])};
				tuples.add(t);
			}
			model.table(portee,tuples).post();	
		}
		in.readLine();
		return model;
	}	
	
	private static double tauxSol(String f,int nbRes) throws Exception{
		int nbSat = 0;
		Solver getSolv;
		String temps = "10s";
		
		BufferedReader readFile = new BufferedReader(new FileReader(f));
		for(int nb=1 ; nb<=nbRes; nb++) {
			//Lecture du réseau nb
			Model model=lireReseau(readFile);
			if(model==null) {
				System.out.println("Problème de lecture de fichier !\n");
				return -1;
			} else {
				getSolv = model.getSolver();
			}
			
			//Recherche des solutions du modèles nb
			getSolv.limitTime(temps);
			if (getSolv.solve()){nbSat++;} 
			else if (getSolv.isStopCriterionMet()){
				System.out.println("Le solveur n'a pu trouver aucune solution en moins de "+temps+".");
			} else {
				//System.out.println("Le solveur a tout exploré et n'a trouvé aucune solution.");
			}
		}
		return (double)nbSat/(double)nbRes*100.0;
	}
	
	//durete = dureté maximal jusqu'à laquelle csvGenerateur va chercher (val possibles : 10 à 110 de 10 en 10)
	//nbRes = nombre de réseaux par CSP exploré (val possibles : 3, 10 et 15) 
	private static void csvGenerateur(int densite,int nbTuple,int nbRes) throws Exception {
	    
		//Initialisation du fichier CSV
	    File fCSV = new File("benchmark/CSV"+densite+"dens"+nbRes+"res.csv");
	    BufferedWriter bwCSV = new BufferedWriter(new FileWriter(fCSV));
	    bwCSV.write("Durete,Taux de reseaux satisfaits");
	    bwCSV.newLine();
	    
	    //Remplissage du fichier CSV
	    int t=nbTuple;
	    String f;
	    while(t>=10) {
	    	//System.out.println("Progression : "+((nbTuple*2-t)/10+1)+"/"+nbTuple*2/10+" - Temps Restant : "+afficheTemps(10*nbRes*t/10));
	    	f = "CSP/densite "+densite+" "+densite+"/csp"+t+"tuples-"+nbRes+"res.txt";
	    	bwCSV.write((nbTuple-t+5)/5+","+tauxSol(f,nbRes)); //durete,taux de réseaux satisfaits
	    	bwCSV.newLine();
	        t-=5;
	    }
	    bwCSV.close();
	}
			
	
	
	public static void main(String[] args) throws Exception{
		int nbRes=3;
		
		
		System.out.println("<-------------------TEST DE BENCH------------------------->");
		String ficName = "bench.txt";
		BufferedReader readFile = new BufferedReader(new FileReader(ficName));
		for(int nb=1 ; nb<=nbRes; nb++) {
			Model model=lireReseau(readFile);
			if(model==null) {
				System.out.println("Problème de lecture de fichier !\n");
				return;
			}
			System.out.println("Réseau lu "+nb+" :\n"+model.getSolver().solve()+"\n\n");
		}
		
		System.out.println("<-------------------TEST DE BENCHSATISF ET BENCHINSAT------------------------->");
		
		//Test du programme sur les fichiers "benchSatisf" et "benchInsat"
		String [] file = {"benchSatisf.txt","benchInsat.txt"};
		int nbSat;
		for (String f : file) {
			BufferedReader buff = new BufferedReader(new FileReader(f));
			nbSat = 0;
			for(int nb=1 ; nb<=nbRes; nb++) {
				Model model=lireReseau(buff);
				if(model==null) {
					System.out.println("Problème de lecture de fichier !\n");
					return;
				}
				if (model.getSolver().findSolution() != null) {nbSat++;} //Le modèle a-t-il une solution ?
			}
			System.out.println(f + " -> " + nbSat + " réseaux satisfaits soit "+tauxSol(f,nbRes)+"%.");
		}
		
		System.out.println();
		System.out.println("<-------------------TEST DE CSP--------------------------->");
		/*try (Scanner entree = new Scanner(System.in)) {
			System.out.println("Veuillez entrer la densité du réseau étudié.");
			System.out.println("val possibles : 10,20,50,100.");
			int densiteCSV = entree.nextInt();
			
			System.out.println("Veuillez entrer le nombre de tuple maximal jusqu'à laquelle csvGenerateur va chercher.");
			System.out.println("val possibles : 10,20,30,40,50,60,70,80,90,100,110.");
			int nbTupleCSV = entree.nextInt();
			
			System.out.println("Veuillez entrer le nombre de réseaux souhaité par CSP");
			System.out.println("val possibles : 3,10,20 et 50");
			int nbResCSV = entree.nextInt();
			
			csvGenerateur(densiteCSV,nbTupleCSV,nbResCSV);
			System.out.println("Fichier CSV généré avec succès.");
		}*/
		
		int nbTupleCSV = 110;
		int [] tabDens = {10,20,50,100};
		int [] tabRes = {3,10,20,50};
		double diz=0.0,un;
		for(int densiteCSV : tabDens) {
			un=0.0;
			for(int nbResCSV : tabRes) {
				csvGenerateur(densiteCSV,nbTupleCSV,nbResCSV);
				un += 25.0/4.0;
				System.out.println("Génération : "+(diz+un)+"%.");
			}
			diz+=25.0;
		}
		System.out.println("Fichiers CSV générés avec succès.");
	}
	
	
	
	
}