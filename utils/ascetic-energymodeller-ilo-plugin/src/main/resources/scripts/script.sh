# sed -n "/oemhp_PresentPower=\(.*\)Watt\(.*\)/p"
# grep -Po 'oemhp_PresentPower=\K[^"]*Watt'
power=$( ./alternative.sh | grep -Po '(?<=(oemhp_PresentPower=)).*(?= Watts)')
echo "${power}"
#echo "Power: ${power} Time: `date`"
