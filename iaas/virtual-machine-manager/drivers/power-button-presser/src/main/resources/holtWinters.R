library("forecast")
args <- commandArgs(TRUE)
data <- as.integer(read.table(args[1], sep=","))
data_holt_winters <- HoltWinters(data, beta=FALSE, gamma=FALSE)
data_forecast <- forecast.HoltWinters(data_holt_winters, h=1)
data_forecast
