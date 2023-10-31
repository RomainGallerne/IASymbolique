import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;

import org.jgrapht.alg.util.Pair;

//Avant dernier = dureté
//Dernier = densité
/* Grand B

for i in {10..100..10}
do
	./urbcsp 10 15 10 $i 10 > csp$i.txt
done
 
*/

public class ExpeAvance {

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
	
	private static boolean hasSolution(Model model) {
		Solver solver = model.getSolver();
		Solution sol = solver.findSolution();
		if (sol != null) {
			System.out.println(sol + "\n");
		}
		return (sol != null);
	}
	
	private static int Taux(String nomFichier,int nbreReseaux) throws Exception{
		int nbReseauSat = 0;
		BufferedReader readFile = new BufferedReader(new FileReader(nomFichier));
		for(int nb=1 ; nb<=nbreReseaux; nb++) { //Pour chaque réseau
			Model model=lireReseau(readFile);
			if(model==null) {
				System.out.println("Problème de lecture de fichier !\n");
				return -1;
			}
			
			Solver solver = model.getSolver();
			String limite = "10s";
			solver.limitTime(limite);
			
			if(solver.solve()){
				nbReseauSat++;
			} else if (solver.isStopCriterionMet()){
				System.out.println("Le solveur n'a pas trouvé s'il y avait une solution ou pas avant d'atteindre la limite de temps ("+limite+").");
			} else {
				System.out.println("Le solveur a trouvé qu'il n'y a pas de solution.");
			}
		}
		
		return (nbReseauSat/nbreReseaux)*100;
	}
	
	private static void toCSV(int nbreReseaux) throws Exception {
		//PARTIE B
		ArrayList<Pair<Integer,Integer>> resultat = new ArrayList<Pair<Integer, Integer>>();
		for (int i = 10; i<=100; i+=10) {
			String nomFichier = "csp"+i+".txt";
			resultat.add(new Pair<Integer,Integer>(i,Taux(nomFichier,nbreReseaux)));
		}
		File file = new File("CSV.csv");
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.write("Dureté,Densité");
		bw.newLine();
		for (int i = 0; i < resultat.size(); i++) {
		    bw.write(resultat.get(i).getFirst());
		    bw.write(",");
		    bw.write(resultat.get(i).getSecond());
		    bw.newLine();
		}
		
		bw.close();
		fw.close();
	}
	
		
			
	public static void main(String[] args) throws Exception{
		/*	PARTIE A
		String ficName = "bench.txt";
		int nbRes = 3;
		BufferedReader readFile = new BufferedReader(new FileReader(ficName));
		for(int nb=1 ; nb<=nbRes; nb++) {
			Model model=lireReseau(readFile);
			if(model==null) {
				System.out.println("Problème de lecture de fichier !\n");
				return;
			}
			System.out.println("Réseau lu "+nb+" :\n"+model+"\n\n");
		}
		*/
		
		// PARTIE A (à partir du 6.)
		int nbRes = 3;
		String [] nomFichier = {"benchSatisf.txt", "benchInsat.txt"};
		for (int i = 0; i < 2; i++) {
			int nbReseauSat = 0;
			String sat = nomFichier[i];
			BufferedReader readFile = new BufferedReader(new FileReader(sat));
			for(int nb=1 ; nb<=nbRes; nb++) {
				Model model=lireReseau(readFile);
				if(model==null) {
					System.out.println("Problème de lecture de fichier !\n");
					return;
				}
				System.out.println("Réseau lu "+nb+" :\n"+model+"\n\n");
				if (hasSolution(model)) {
					nbReseauSat++;
				}
			}
			System.out.println("Nombre de réseaux satisfaits de " + sat + ": " + nbReseauSat);
		}
		
		nbRes = 10;
		toCSV(nbRes);
		return;
	}
}
