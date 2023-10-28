import java.util.Scanner;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.constraints.extension.Tuples;

public class nReines {
public static void main(String[] args) {
		
	 	/***********************************************************************
     	*                        Création du Modèle                            *
     	***********************************************************************/
		Model model = new Model("nReines");
		
		Scanner sc = new Scanner(System.in);
		System.out.println("Veuillez saisir n :");
		int n = sc.nextInt();
		
		/***********************************************************************
        *                     Création des variables                           *
        ***********************************************************************/
		IntVar [] R = model.intVarArray("x",n,1,n);   

	    
		/***********************************************************************
        *                   Création des contraintes                           *
        ***********************************************************************/
		model.allDifferent(R).post(); //Toutes les reines sont sur des colones différentes
        
        for(int i=0;i<n;i++) {
        	for(int j=0;j<n;j++) {
        		if(i!=j) {
        			model.distance(R[i],R[j],"!=",Math.abs(i-j)).post();
        		}
        	}
        }
        
        
        // Affichage du réseau de contraintes créé
        System.out.println("*** Réseau Initial ***");
        System.out.println(model);
        

        // Calcul de la première solution
        if(model.getSolver().solve()) {
        	System.out.println("\n\n*** Première solution ***");        
        	System.out.println(model);
        }

            
    	// Calcul de toutes les solutions
    	System.out.println("\n\n*** Autres solutions ***");        
        while(model.getSolver().solve()) {    	
            System.out.println("Sol "+ model.getSolver().getSolutionCount()+"\n"+model);
	    }  
 
        
        // Affichage de l'ensemble des caractéristiques de résolution
      	System.out.println("\n\n*** Bilan ***");        
        model.getSolver().printStatistics();
	}
}
