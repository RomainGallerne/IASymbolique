for %%x in (10 15 20 25 30 35 40 45 50 55 60 65 70 75 80 85 90 95 100 105 110) do (
	for %%y in (3 10 20 50) do (
		urbcsp 100 15 100 %%x %%y > "CSP"/"densite 100 100"/"csp%%xtuples-%%yres.txt"
	)
)