src <- "run_00004/" #the source directory
filePath <- paste(src,"/lifeCyclingWithoutUnits.csv", sep = "")
data <- read.csv(filePath)

#install.packages("ggplot2") #if not installed
#coord_cartesian(xlim = c(5000, 7000)) +        #considers all data for further calcs
#scale_x_continuous(limits = c(5000,7000))+     #sets data out of range to NA
#geom_something(aes(y=value, colour = "Name"))  #makes the value accessible with name
#scale_colour_manual("", values = c("Name"="Colour",...) #maps colour to name
data_averages <- data.frame(matrix(0, ncol = 1, nrow = max(data$days) + 1))
data_averages$days <- seq(0, max(data$days), 1)
data_averages$mean_Biomass <- tapply(data$Biomass, data$days, mean)
data_averages$mean_IngestedEnergy <- tapply(data$Ingested_Energy, data$days, mean)
data_averages$mean_Netenergy <- tapply(data$Netenergy, data$days, mean)
data_averages$mean_ConsumedEnergy <- tapply(data$Consumed_Energy, data$days, mean)
data_averages$mean_age <- tapply(data$AGE, data$days, mean)
data_averages$mean_length <- tapply(data$Length, data$days, mean)

library(ggplot2)

energyPlot <- ggplot(data = data_averages, aes(x = days)) +
    geom_line(aes(y = mean_IngestedEnergy, colour = "IngestedEnergy"), size = 0.5) +
    geom_line(aes(y = mean_Netenergy, colour = "NetEnergy"), size = 0.5) +
    geom_line(aes(y = mean_ConsumedEnergy, colour = "ConsumedEnergy"), size = 0.5) +
    scale_colour_manual("",
                       values = c("IngestedEnergy"="darkgreen",
                       "NetEnergy"="darkblue",
                       "ConsumedEnergy" = "darkred")) +
                       ylab("kJ") +
                       xlab("Days")
#print(last_plot())

ggsave(filename = "AverageEnergy.png", plot = last_plot(), path = src, units = "cm", width = 25, height = 15, dpi = 200)

agePlot <- ggplot(data = data_averages, aes(x = days)) +
    geom_point(aes(y = mean_age, colour = "agePoint"), size=0.6) +
    geom_smooth(aes(y = mean_age, colour = "age")) +
    scale_colour_manual("",
                       values = c("age" = "red", "agePoint" = "darkblue")) +
                       ylab("Years") +
                       xlab("Days")
#print(last_plot())
ggsave(filename = "AverageAge.png", plot = last_plot(), path = src, units = "cm", width = 25, height = 15, dpi = 200)

lengthPlot <- ggplot(data = data_averages, aes(x = days)) +
    geom_point(aes(y = mean_length, colour = "lengthPoint"), size = 0.6) +
    geom_smooth(aes(y = mean_length, colour = "length")) +
    scale_colour_manual("",
                       values = c("length" = "red", "lengthPoint" = "darkblue")) +
                       ylab("cm") +
                       xlab("Days")

#print(last_plot())
ggsave(filename = "AverageLength.png", plot = last_plot(), path = src, units = "cm", width = 25, height = 15, dpi = 200)

filteredDeaths <- (data$Cause_of_Death)
filteredDeaths <- droplevels(filteredDeaths, "NONE")
filteredDeaths <- data.frame(filteredDeaths)
filteredDeaths <- na.omit(filteredDeaths)
deathPlot <- ggplot(filteredDeaths, aes(filteredDeaths)) + geom_bar(fill = "steelblue") +
    geom_text(stat = 'count', aes(label = ..count..), vjust = -1)

#print(last_plot())
ggsave(filename = "DeathCauses.png", plot = last_plot(), path = src, units = "cm", width = 12, height = 20, dpi = 200)

populationData <- with(data, table(days, Phase)) #use length(levels(as.factor(data$days)))) if you go for all days
populationData <- as.data.frame.matrix(populationData)
populationData <- cbind(rownames(populationData), populationData)
colnames(populationData)[1] <- "days"
#head(populationData, 5)
populationData$days <- as.numeric(as.character(populationData$days))

populationPlot <- ggplot(data = populationData, aes(x = days)) +
    geom_line(aes(y = populationData$INITIAL, colour = "Initial"), size = 0.4) +
    geom_point(aes(y = populationData$INITIAL, colour = "Initial"), size = 0.7) +
    geom_line(aes(y = populationData$JUVENILE, colour = "Juvenile"), size = 0.4) +
    geom_point(aes(y = populationData$JUVENILE, colour = "Juvenile"), size = 0.7) +
    geom_line(aes(y = populationData$TERMINAL, colour = "Terminal"), size = 0.4) +
    geom_point(aes(y = populationData$TERMINAL, colour = "Terminal"), size = 0.7) +
    geom_line(aes(y = populationData$DEAD, colour = "Dead"), size = 0.3) +
    scale_colour_manual("",
                       values = c("Initial" = "red",
                       "Juvenile" = "blue",
                       "Terminal" = "green",
                       "Dead" = "black")) +
                       ylab("Count") +
                       xlab("Days")

#print(last_plot())
ggsave(filename = "Population.png", plot = last_plot(), path = src, units = "cm", width = 30, height = 15, dpi = 200)