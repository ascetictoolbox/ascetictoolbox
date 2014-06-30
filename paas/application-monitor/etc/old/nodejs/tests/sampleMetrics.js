module.exports = { "applicationId" : "Some Batch App",
	"startTime" : 1399976840925,
	"endTime" : 1399976860444,
	"nodes" : {
		"MasterNode" : {
			"iostat" : {
				"load" : { "15m" : "1.41", "5m" : "1.29", "1m" : "1.22" },
				"cpu" : { "id" : "90", "sy" : "3", "us" : "7" },
				"disk0" : { "MB/s" : "0.40", "tps" : "17", "KB/t" : "24.81" } },
			"ps" :
			{ "command" : "./executable --param", "time" : "0:00.27", "started" : "6:28PM",
				"stat" : "S+", "tt" : "s005", "rss" : "31224", "vsz" : "3052940",
				"%mem" : "0.4", "%cpu" : "0.0", "pid" : "4096", "user" : "mmacias" } },
		"SlaveNode" : {
			"iostat" : {
				"load" : { "15m" : "1.41", "5m" : "1.29", "1m" : "1.22" },
				"cpu" : { "id" : "90", "sy" : "3", "us" : "7" },
				"disk0" : { "MB/s" : "0.40", "tps" : "17", "KB/t" : "24.81" } },
			"ps" :
			{ "command" : "./executable --param --slave", "time" : "0:00.27", "started" : "6:28PM",
				"stat" : "S+", "tt" : "s005", "rss" : "31224", "vsz" : "3052940",
				"%mem" : "0.4", "%cpu" : "0.0", "pid" : "4096", "user" : "mmacias" } }}};
