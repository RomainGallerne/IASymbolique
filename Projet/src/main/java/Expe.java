import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;

public class Expe {
	
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
	
	private static void csvGenerateur(int nbContraintes,int nbTuples,int nbRes) throws Exception {
	    
		//Initialisation du fichier CSV
	    File fCSV = new File("benchmark/densite-"+Math.round((double)nbContraintes*100.0/300.0)+"/CSV"+nbTuples+"tuples"+nbRes+"res.csv");
	    BufferedWriter bwCSV = new BufferedWriter(new FileWriter(fCSV));
	    bwCSV.write("Nombre de tuples,Taux de reseaux satisfaits");
	    bwCSV.newLine();
	    
	    //Remplissage du fichier CSV
	    int t=50;
	    String f;
	    while(t<=nbTuples) {
	    	f = "CSP/nbContraintes-"+nbContraintes+"/csp-25-40-"+nbContraintes+"-"+nbTuples+"-"+nbRes+".txt";
	    	bwCSV.write(t+","+tauxSol(f,nbRes)); //nbTuples,taux de réseaux satisfaits
	    	bwCSV.newLine();
	        t+=50;
	    }
	    bwCSV.close();
	}
	
	private static void genToutCSV(int[] tabContraintes, int nbTuples, int[] tabRes) throws Exception{
		//Génération de tous les CSV possibles
		int progression = 0;
		for(int nbContr : tabContraintes) {
			System.out.println("-> Génération des CSV de densité "+Math.round((double)nbContr*100.0/300.0));
			for(int nbRes : tabRes) {
				csvGenerateur(nbContr,nbTuples,nbRes);
				progression ++;
				System.out.println("	Progression : "+(progression*100)/(tabContraintes.length*tabRes.length)+"%.");
			}
		}
		System.out.println("Tous les fichiers CSV ont été générés avec succès.");
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
	
			System.out.println("Veuillez entrer le nombre de tuple maximal jusqu'à laquelle csvGenerateur va chercher.");
			System.out.println("val possibles : 10,20,30,40,50,60,70,80,90,100,110.");
			int nbTupleCSV = entree.nextInt();
			
			System.out.println("Veuillez entrer le nombre de réseaux souhaité par CSP");
			System.out.println("val possibles : 3,10,20 et 50");
			int nbResCSV = entree.nextInt();
			
			csvGenerateur(nbTupleCSV,nbResCSV);
			System.out.println("Fichier CSV généré avec succès.");
		}*/
		
		int nbTuplesCSV = 1550;
		
		//Valeurs de tabRes possibles : {3,10,15,20}
		//int [] tabRes = {3,10,15};
		int [] tabRes = {3};
		
		//Niveau de dureté 25%, 50%, 75
		//int [] tabContraintes = {75,150,225};
		int [] tabContraintes = {150};
		
		genToutCSV(tabContraintes,nbTuplesCSV,tabRes);
		
		
	}
	
	
	
	
}