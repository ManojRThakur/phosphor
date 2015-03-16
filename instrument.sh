if [ "$1" == "batik" ]; then
	less Phosphor/partial-inst/batik_additional >> methods
elif [ "$1" == "pmd" ]; then
	less Phosphor/partial-inst/pmd_additional >> methods
elif ["$1" == "lusearch" ]; then
	less Phosphor/partial-inst/lusearch_additional >> methods
fi

java -jar phosphor_pi.jar $1 $1-inst
