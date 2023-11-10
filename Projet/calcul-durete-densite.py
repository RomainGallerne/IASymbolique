#calcul de densit40é
#nbVariables = int(input("Nombre de variables (baisse la densite) : "))
#nbContraintes = int(input("Nombre de contraintes (augmente la densite) : "))

#densite = round(((2*nbContraintes)/((nbVariables**2)-nbVariables)) * 100)
#print("Densite : ",densite,"%.\n")

#calcul de dureté
tailleDomaine = int(input("Taille du domaine (augmente la durete) : "))
nbTuples = int(input("Nombre de tuples (baisse la durete) : "))

durete = round(((tailleDomaine**2-nbTuples)/(tailleDomaine**2)) * 100)
print("Dureté : ",durete,"%.\n")
