src <- "nonInverted/run_" #the source directory
runs <- c("00005", "00011", "00017")
#saveDir <- paste0("inverted_", paste(runs, collapse = "_")) # use this if you have no specific name in mind
saveDir <- "nonInverted_MapC_RANDOM"
dir.create(file.path(saveDir))
z = 1.96 # z = 1,645 (90%), z = 1,96 (95%), lower = 1.96*(sd(list)/sqrt(n)), upper = mean + z*(sd(list)/sqrt(n))


# Pre setup, without this, the first data set would be missing for whatever reason ------------------
filePath <- paste0(paste0(src, runs[1]), "/lifeCyclingWithoutUnits.csv")
data <- read.csv(filePath, sep = ";")
data_juvenile <- data[which(data$Phase == "JUVENILE"),]
data_initial <- data[which(data$Phase == "INITIAL"),]
data_terminal <- data[which(data$Phase == "TERMINAL"),]

# Setup  ------------------------------------
# some things seem a bit duplicate, but it was the easiest solution
# and other solutions would often do weird stuff
if (length(runs) >= 2) {
    for (i in 2:length(runs)) {
    filePath <- paste0(paste0(src, runs[i]), "/lifeCyclingWithoutUnits.csv")
    dataTMP <- read.csv(filePath, sep = ";")
    tmp_juvenile <- dataTMP[which(dataTMP$Phase == "JUVENILE"),]
    tmp_initial <- dataTMP[which(dataTMP$Phase == "INITIAL"),]
    tmp_terminal <- dataTMP[which(dataTMP$Phase == "TERMINAL"),]
    data <- rbind(data, dataTMP)
    print(filePath)
}
}

data_juvenile <- data[which(data$Phase == "JUVENILE"),]
data_initial <- data[which(data$Phase == "INITIAL"),]
data_terminal <- data[which(data$Phase == "TERMINAL"),]
data_dead <- data[which(data$Cause_of_Death != "NONE"),]

#day count equals population on that day
data_averages <- data.frame(matrix(0, ncol = 1, nrow = max(data$days) + 1))
data_averages$days <- seq(0, max(data$days), 1)
data_averages$juvDAYS <- setNames(as.data.frame(table(factor(data_juvenile$days, levels = min(data$days):max(data$days)))), c("Day", "count"))
data_averages$juvDAYS$Day <- as.numeric(levels(data_averages$juvDAYS$Day))[data_averages$juvDAYS$Day]
data_averages$initDAYS <- setNames(as.data.frame(table(factor(data_initial$days, levels = min(data$days):max(data$days)))), c("Day", "count"))
data_averages$initDAYS$Day <- as.numeric(levels(data_averages$initDAYS$Day))[data_averages$initDAYS$Day]
data_averages$termDAYS <- setNames(as.data.frame(table(factor(data_terminal$days, levels = min(data$days):max(data$days)))), c("Day", "count"))
data_averages$termDAYS$Day <- as.numeric(levels(data_averages$termDAYS$Day))[data_averages$termDAYS$Day]


# Population --------------------
data_averages$population.juv <- tapply(data_averages$juvDAYS$count, data_averages$juvDAYS$Day, mean)
data_averages$population.juvSD <- tapply(data_averages$juvDAYS$count, data_averages$juvDAYS$Day, sd)
data_averages$population.init <- tapply(data_averages$initDAYS$count, data_averages$initDAYS$Day, mean)
data_averages$population.initSD <- tapply(data_averages$initDAYS$count, data_averages$initDAYS$Day, sd)
data_averages$population.term <- tapply(data_averages$termDAYS$count, data_averages$termDAYS$Day, mean)
data_averages$population.termSD <- tapply(data_averages$termDAYS$count, data_averages$termDAYS$Day, sd)

#tmp mean ---------------------
tmp <- data.frame(matrix(0:max(data$days), ncol = 1, nrow = max(data$days) + 1))
colnames(tmp) <- c("Day")
tmp <- merge(tmp, setNames(aggregate(data_juvenile$Biomass.g., list(data_juvenile$days), mean), c("Day", "Biomass_juv")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Biomass.g., list(data_initial$days), mean), c("Day", "Biomass_init")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Biomass.g., list(data_terminal$days), mean), c("Day", "Biomass_term")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Repro_Storage.kJ., list(data_juvenile$days), mean), c("Day", "Repro_juv")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Repro_Storage.kJ., list(data_initial$days), mean), c("Day", "Repro_init")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Repro_Storage.kJ., list(data_terminal$days), mean), c("Day", "Repro_term")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Gut.kJ., list(data_juvenile$days), mean), c("Day", "Gut_juv")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Gut.kJ., list(data_initial$days), mean), c("Day", "Gut_init")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Gut.kJ., list(data_terminal$days), mean), c("Day", "Gut_term")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Protein.kJ., list(data_juvenile$days), mean), c("Day", "Protein_juv")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Protein.kJ., list(data_initial$days), mean), c("Day", "Protein_init")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Protein.kJ., list(data_terminal$days), mean), c("Day", "Protein_term")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Fat.kJ., list(data_juvenile$days), mean), c("Day", "Fat_juv")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Fat.kJ., list(data_initial$days), mean), c("Day", "Fat_init")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Fat.kJ., list(data_terminal$days), mean), c("Day", "Fat_term")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Excess.kJ., list(data_juvenile$days), mean), c("Day", "Excess_juv")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Excess.kJ., list(data_initial$days), mean), c("Day", "Excess_init")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Excess.kJ., list(data_terminal$days), mean), c("Day", "Excess_term")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Shorrterm, list(data_juvenile$days), mean), c("Day", "Shortterm_juv")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Shorrterm, list(data_initial$days), mean), c("Day", "Shortterm_init")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Shorrterm, list(data_terminal$days), mean), c("Day", "Shortterm_term")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Ingested_Energy.kJ., list(data_juvenile$days), mean), c("Day", "Ingested_juv")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Ingested_Energy.kJ., list(data_initial$days), mean), c("Day", "Ingested_init")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Ingested_Energy.kJ., list(data_terminal$days), mean), c("Day", "Ingested_term")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Netenergy.kJ., list(data_juvenile$days), mean), c("Day", "Net_juv")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Netenergy.kJ., list(data_initial$days), mean), c("Day", "Net_init")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Netenergy.kJ., list(data_terminal$days), mean), c("Day", "Net_term")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Consumed_Energy.kJ., list(data_juvenile$days), mean), c("Day", "Consumed_juv")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Consumed_Energy.kJ., list(data_initial$days), mean), c("Day", "Consumed_init")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Consumed_Energy.kJ., list(data_terminal$days), mean), c("Day", "Consumed_term")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$AGE.years., list(data_juvenile$days), mean), c("Day", "Age_juv")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$AGE.years., list(data_initial$days), mean), c("Day", "Age_init")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$AGE.years., list(data_terminal$days), mean), c("Day", "Age_term")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Length.cm., list(data_juvenile$days), mean), c("Day", "Length_juv")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Length.cm., list(data_initial$days), mean), c("Day", "Length_init")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Length.cm., list(data_terminal$days), mean), c("Day", "Length_term")), by = "Day", all = TRUE)

#tmp-SD ----------------------
tmp <- merge(tmp, setNames(aggregate(data_juvenile$Biomass.g., list(data_juvenile$days), sd), c("Day", "Biomass_juvSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Biomass.g., list(data_initial$days), sd), c("Day", "Biomass_initSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Biomass.g., list(data_terminal$days), sd), c("Day", "Biomass_termSD")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Repro_Storage.kJ., list(data_juvenile$days), sd), c("Day", "Repro_juvSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Repro_Storage.kJ., list(data_initial$days), sd), c("Day", "Repro_initSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Repro_Storage.kJ., list(data_terminal$days), sd), c("Day", "Repro_termSD")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Gut.kJ., list(data_juvenile$days), sd), c("Day", "Gut_juvSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Gut.kJ., list(data_initial$days), sd), c("Day", "Gut_initSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Gut.kJ., list(data_terminal$days), sd), c("Day", "Gut_termSD")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Protein.kJ., list(data_juvenile$days), sd), c("Day", "Protein_juvSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Protein.kJ., list(data_initial$days), sd), c("Day", "Protein_initSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Protein.kJ., list(data_terminal$days), sd), c("Day", "Protein_termSD")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Fat.kJ., list(data_juvenile$days), sd), c("Day", "Fat_juvSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Fat.kJ., list(data_initial$days), sd), c("Day", "Fat_initSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Fat.kJ., list(data_terminal$days), sd), c("Day", "Fat_termSD")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Excess.kJ., list(data_juvenile$days), sd), c("Day", "Excess_juvSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Excess.kJ., list(data_initial$days), sd), c("Day", "Excess_initSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Excess.kJ., list(data_terminal$days), sd), c("Day", "Excess_termSD")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Shorrterm, list(data_juvenile$days), sd), c("Day", "Shortterm_juvSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Shorrterm, list(data_initial$days), sd), c("Day", "Shortterm_initSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Shorrterm, list(data_terminal$days), sd), c("Day", "Shortterm_termSD")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Ingested_Energy.kJ., list(data_juvenile$days), sd), c("Day", "Ingested_juvSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Ingested_Energy.kJ., list(data_initial$days), sd), c("Day", "Ingested_initSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Ingested_Energy.kJ., list(data_terminal$days), sd), c("Day", "Ingested_termSD")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Netenergy.kJ., list(data_juvenile$days), sd), c("Day", "Net_juvSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Netenergy.kJ., list(data_initial$days), sd), c("Day", "Net_initSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Netenergy.kJ., list(data_terminal$days), sd), c("Day", "Net_termSD")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Consumed_Energy.kJ., list(data_juvenile$days), sd), c("Day", "Consumed_juvSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Consumed_Energy.kJ., list(data_initial$days), sd), c("Day", "Consumed_initSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Consumed_Energy.kJ., list(data_terminal$days), sd), c("Day", "Consumed_termSD")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$AGE.years., list(data_juvenile$days), sd), c("Day", "Age_juvSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$AGE.years., list(data_initial$days), sd), c("Day", "Age_initSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$AGE.years., list(data_terminal$days), sd), c("Day", "Age_termSD")), by = "Day", all = TRUE)

tmp <- merge(tmp, setNames(aggregate(data_juvenile$Length.cm., list(data_juvenile$days), sd), c("Day", "Length_juvSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_initial$Length.cm., list(data_initial$days), sd), c("Day", "Length_initSD")), by = "Day", all = TRUE)
tmp <- merge(tmp, setNames(aggregate(data_terminal$Length.cm., list(data_terminal$days), sd), c("Day", "Length_termSD")), by = "Day", all = TRUE)

# Compartments -------------------------------------
#Biomass
data_averages$mean_Biomass.juv <- tmp$Biomass_juv
data_averages$mean_Biomass.juvSD <- tmp$Biomass_juvSD
data_averages$mean_Biomass.init <- tmp$Biomass_init
data_averages$mean_Biomass.initSD <- tmp$Biomass_initSD
data_averages$mean_Biomass.term <- tmp$Biomass_term
data_averages$mean_Biomass.termSD <- tmp$Biomass_termSD

#Reproduction_Storage
data_averages$mean_Repro.juv <- tmp$Repro_juv
data_averages$mean_Repro.juvSD <- tmp$Repro_juvSD
data_averages$mean_Repro.init <- tmp$Repro_init
data_averages$mean_Repro.initSD <- tmp$Repro_initSD
data_averages$mean_Repro.term <- tmp$Repro_term
data_averages$mean_Repro.termSD <- tmp$Repro_termSD

#Gut
data_averages$mean_Gut.juv <- tmp$Gut_juv
data_averages$mean_Gut.juvSD <- tmp$Gut_juvSD
data_averages$mean_Gut.init <- tmp$Gut_init
data_averages$mean_Gut.initSD <- tmp$Gut_initSD
data_averages$mean_Gut.term <- tmp$Gut_term
data_averages$mean_Gut.termSD <- tmp$Gut_termSD

#Protein
data_averages$mean_Protein.juv <- tmp$Protein_juv
data_averages$mean_Protein.juvSD <- tmp$Protein_juvSD
data_averages$mean_Protein.init <- tmp$Protein_init
data_averages$mean_Protein.initSD <- tmp$Protein_initSD
data_averages$mean_Protein.term <- tmp$Protein_term
data_averages$mean_Protein.termSD <- tmp$Protein_termSD

#Fat
data_averages$mean_Fat.juv <- tmp$Fat_juv
data_averages$mean_Fat.juvSD <- tmp$Fat_juvSD
data_averages$mean_Fat.init <- tmp$Fat_init
data_averages$mean_Fat.initSD <- tmp$Fat_initSD
data_averages$mean_Fat.term <- tmp$Fat_term
data_averages$mean_Fat.termSD <- tmp$Fat_termSD

#Excess
data_averages$mean_Excess.juv <- tmp$Excess_juv
data_averages$mean_Excess.juvSD <- tmp$Excess_juvSD
data_averages$mean_Excess.init <- tmp$Excess_init
data_averages$mean_Excess.initSD <- tmp$Excess_initSD
data_averages$mean_Excess.term <- tmp$Excess_term
data_averages$mean_Excess.termSD <- tmp$Excess_termSD

#Shorrterm
data_averages$mean_Shortterm.juv <- tmp$Shortterm_juv
data_averages$mean_Shortterm.juvSD <- tmp$Shortterm_juvSD
data_averages$mean_Shortterm.init <- tmp$Shortterm_init
data_averages$mean_Shortterm.initSD <- tmp$Shortterm_initSD
data_averages$mean_Shortterm.term <- tmp$Shortterm_term
data_averages$mean_Shortterm.termSD <- tmp$Shortterm_termSD

#Ingested 
data_averages$mean_Ingested.juv <- tmp$Ingested_juv
data_averages$mean_Ingested.juvSD <- tmp$Ingested_juvSD
data_averages$mean_Ingested.init <- tmp$Ingested_init
data_averages$mean_Ingested.initSD <- tmp$Ingested_initSD
data_averages$mean_Ingested.term <- tmp$Ingested_term
data_averages$mean_Ingested.termSD <- tmp$Ingested_termSD

#Net
data_averages$mean_Net.juv <- tmp$Net_juv
data_averages$mean_Net.juvSD <- tmp$Net_juvSD
data_averages$mean_Net.init <- tmp$Net_init
data_averages$mean_Net.initSD <- tmp$Net_initSD
data_averages$mean_Net.term <- tmp$Net_term
data_averages$mean_Net.termSD <- tmp$Net_termSD

#Consumed
data_averages$mean_Consumed.juv <- tmp$Consumed_juv
data_averages$mean_Consumed.juvSD <- tmp$Consumed_juvSD
data_averages$mean_Consumed.init <- tmp$Consumed_init
data_averages$mean_Consumed.initSD <- tmp$Consumed_initSD
data_averages$mean_Consumed.term <- tmp$Consumed_term
data_averages$mean_Consumed.termSD <- tmp$Consumed_termSD

# Age and Length -----------------------------------------------
#Age
data_averages$mean_age.juv <- tmp$Age_juv
data_averages$mean_age.juvSD <- tmp$Age_juvSD
data_averages$mean_age.init <- tmp$Age_init
data_averages$mean_age.initSD <- tmp$Age_initSD
data_averages$mean_age.term <- tmp$Age_term
data_averages$mean_age.termSD <- tmp$Age_termSD

#Length
data_averages$mean_length.juv <- tmp$Length_juv
data_averages$mean_length.juvSD <- tmp$Length_juvSD
data_averages$mean_length.init <- tmp$Length_init
data_averages$mean_length.initSD <- tmp$Length_initSD
data_averages$mean_length.term <- tmp$Length_term
data_averages$mean_length.termSD <- tmp$Length_termSD


# Plots -------------------------------------------------------------
library(ggplot2)



energyPlotJuv <- ggplot(data = data_averages, aes(x = days)) +
    geom_ribbon(aes(ymin = mean_Repro.juv - z * (mean_Repro.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    ymax = mean_Repro.juv + z * (mean_Repro.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    fill = "Reprostorage"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Repro.juv, colour = "Reprostorage"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Gut.juv - z * (mean_Gut.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    ymax = mean_Gut.juv + z * (mean_Gut.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    fill = "Gut"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Gut.juv, colour = "Gut"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Protein.juv - z * (mean_Protein.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    ymax = mean_Protein.juv + z * (mean_Protein.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    fill = "Protein"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Protein.juv, colour = "Protein"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Fat.juv - z * (mean_Fat.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    ymax = mean_Fat.juv + z * (mean_Fat.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    fill = "Fat"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Fat.juv, colour = "Fat"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Excess.juv - z * (mean_Excess.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    ymax = mean_Excess.juv + z * (mean_Excess.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    fill = "Excess"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Excess.juv, colour = "Excess"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Shortterm.juv - z * (mean_Shortterm.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    ymax = mean_Shortterm.juv + z * (mean_Shortterm.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    fill = "Shortterm"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Shortterm.juv, colour = "Shortterm"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Ingested.juv - z * (mean_Ingested.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    ymax = mean_Ingested.juv + z * (mean_Ingested.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    fill = "Ingested"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Ingested.juv, colour = "Ingested"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Net.juv - z * (mean_Net.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    ymax = mean_Net.juv + z * (mean_Net.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    fill = "Net"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Net.juv, colour = "Net"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Consumed.juv - z * (mean_Consumed.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    ymax = mean_Consumed.juv + z * (mean_Consumed.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    fill = "Consumed"), alpha = 0.5, show.legend = F) +
    geom_line(aes(y = mean_Consumed.juv, colour = "Consumed"), size = 0.7) +
    scale_colour_manual("",
                       values = c("Reprostorage" = "darkmagenta",
                                  "Gut" = "gold4",
                                  "Protein" = "brown",
                                  "Fat" = "burlywood3",
                                  "Excess" = "darkorange",
                                  "Shortterm" = "goldenrod2",
                                  "Ingested" = "green",
                                  "Net" = "blue",
                                  "Consumed" = "red")) +
    scale_fill_manual("",
                       values = c("Reprostorage" = "darkmagenta",
                                  "Gut" = "gold4",
                                  "Protein" = "brown",
                                  "Fat" = "burlywood3",
                                  "Excess" = "darkorange",
                                  "Shortterm" = "goldenrod2",
                                  "Ingested" = "green",
                                  "Net" = "blue",
                                  "Consumed" = "red")) +
                       ylab("kJ") +
                       xlab("Days")
#print(last_plot())

ggsave(filename = "AverageEnergyJuv.png", plot = last_plot(), path = saveDir, units = "cm", width = 25, height = 15, dpi = 200)

energyPlotInit <- ggplot(data = data_averages, aes(x = days)) +
    geom_ribbon(aes(ymin = mean_Repro.init - z * (mean_Repro.initSD / sqrt(data_averages$initDAYS$Day)),
                    ymax = mean_Repro.init + z * (mean_Repro.initSD / sqrt(data_averages$initDAYS$Day)),
                    fill = "Reprostorage"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Repro.init, colour = "Reprostorage"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Gut.init - z * (mean_Gut.initSD / sqrt(data_averages$initDAYS$Day)),
                    ymax = mean_Gut.init + z * (mean_Gut.initSD / sqrt(data_averages$initDAYS$Day)),
                    fill = "Gut"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Gut.init, colour = "Gut"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Protein.init - z * (mean_Protein.initSD / sqrt(data_averages$initDAYS$Day)),
                    ymax = mean_Protein.init + z * (mean_Protein.initSD / sqrt(data_averages$initDAYS$Day)),
                    fill = "Protein"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Protein.init, colour = "Protein"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Fat.init - z * (mean_Fat.initSD / sqrt(data_averages$initDAYS$Day)),
                    ymax = mean_Fat.init + z * (mean_Fat.initSD / sqrt(data_averages$initDAYS$Day)),
                    fill = "Fat"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Fat.init, colour = "Fat"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Excess.init - z * (mean_Excess.initSD / sqrt(data_averages$initDAYS$Day)),
                    ymax = mean_Excess.init + z * (mean_Excess.initSD / sqrt(data_averages$initDAYS$Day)),
                    fill = "Excess"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Excess.init, colour = "Excess"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Shortterm.init - z * (mean_Shortterm.initSD / sqrt(data_averages$initDAYS$Day)),
                    ymax = mean_Shortterm.init + z * (mean_Shortterm.initSD / sqrt(data_averages$initDAYS$Day)),
                    fill = "Shortterm"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Shortterm.init, colour = "Shorrterm"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Ingested.init - z * (mean_Ingested.initSD / sqrt(data_averages$initDAYS$Day)),
                    ymax = mean_Ingested.init + z * (mean_Ingested.initSD / sqrt(data_averages$initDAYS$Day)),
                    fill = "Ingested"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Ingested.init, colour = "Ingested"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Net.init - z * (mean_Net.initSD / sqrt(data_averages$initDAYS$Day)),
                    ymax = mean_Net.init + z * (mean_Net.initSD / sqrt(data_averages$initDAYS$Day)),
                    fill = "Net"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Net.init, colour = "Net"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Consumed.init - z * (mean_Consumed.initSD / sqrt(data_averages$initDAYS$Day)),
                    ymax = mean_Consumed.init + z * (mean_Consumed.initSD / sqrt(data_averages$initDAYS$Day)),
                    fill = "Consumed"), alpha = 0.5, show.legend = F) +
    geom_line(aes(y = mean_Consumed.init, colour = "Consumed"), size = 0.7) +
    scale_colour_manual("",
                       values = c("Reprostorage" = "darkmagenta",
                                  "Gut" = "gold4",
                                  "Protein" = "brown",
                                  "Fat" = "burlywood3",
                                  "Excess" = "darkorange",
                                  "Shorrterm" = "goldenrod2",
                                  "Ingested" = "darkgreen",
                                  "Net" = "darkblue",
                                  "Consumed" = "darkred")) +
    scale_fill_manual("",
                      values = c("Reprostorage" = "darkmagenta",
                                  "Gut" = "gold4",
                                  "Protein" = "brown",
                                  "Fat" = "burlywood3",
                                  "Excess" = "darkorange",
                                  "Shortterm" = "goldenrod2",
                                  "Ingested" = "green",
                                  "Net" = "blue",
                                  "Consumed" = "red")) +
                                  ylab("kJ") +
                                  xlab("Days")
#print(last_plot())

ggsave(filename = "AverageEnergyInit.png", plot = last_plot(), path = saveDir, units = "cm", width = 25, height = 15, dpi = 200)

energyPlotTerm <- ggplot(data = data_averages, aes(x = days)) +
    geom_ribbon(aes(ymin = mean_Repro.term - z * (mean_Repro.termSD / sqrt(data_averages$termDAYS$Day)),
                    ymax = mean_Repro.term + z * (mean_Repro.termSD / sqrt(data_averages$termDAYS$Day)),
                    fill = "Reprostorage"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Repro.term, colour = "Reprostorage"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Gut.init - z * (mean_Gut.initSD / sqrt(data_averages$termDAYS$Day)),
                    ymax = mean_Gut.init + z * (mean_Gut.initSD / sqrt(data_averages$termDAYS$Day)),
                    fill = "Gut"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Gut.term, colour = "Gut"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Protein.term - z * (mean_Protein.termSD / sqrt(data_averages$termDAYS$Day)),
                    ymax = mean_Protein.term + z * (mean_Protein.termSD / sqrt(data_averages$termDAYS$Day)),
                    fill = "Protein"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Protein.term, colour = "Protein"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Fat.term - z * (mean_Fat.termSD / sqrt(data_averages$termDAYS$Day)),
                    ymax = mean_Fat.term + z * (mean_Fat.termSD / sqrt(data_averages$termDAYS$Day)),
                    fill = "Fat"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Fat.term, colour = "Fat"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Excess.term - z * (mean_Excess.termSD / sqrt(data_averages$termDAYS$Day)),
                    ymax = mean_Excess.term + z * (mean_Excess.termSD / sqrt(data_averages$termDAYS$Day)),
                    fill = "Excess"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Excess.term, colour = "Excess"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Shortterm.term - z * (mean_Shortterm.termSD / sqrt(data_averages$termDAYS$Day)),
                    ymax = mean_Shortterm.term + z * (mean_Shortterm.termSD / sqrt(data_averages$termDAYS$Day)),
                    fill = "Shortterm"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Shortterm.term, colour = "Shortterm"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Ingested.term - z * (mean_Ingested.termSD / sqrt(data_averages$termDAYS$Day)),
                    ymax = mean_Ingested.term + z * (mean_Ingested.termSD / sqrt(data_averages$termDAYS$Day)),
                    fill = "Ingested"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Ingested.term, colour = "Ingested"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Net.term - z * (mean_Net.termSD / sqrt(data_averages$termDAYS$Day)),
                    ymax = mean_Net.term + z * (mean_Net.termSD / sqrt(data_averages$termDAYS$Day)),
                    fill = "Net"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = mean_Net.term, colour = "Net"), size = 0.7) +
                    geom_ribbon(aes(ymin = mean_Consumed.term - z * (mean_Consumed.termSD / sqrt(data_averages$termDAYS$Day)),
                    ymax = mean_Consumed.term + z * (mean_Consumed.termSD / sqrt(data_averages$termDAYS$Day)),
                    fill = "Consumed"), alpha = 0.5, show.legend = F) +
    geom_line(aes(y = mean_Consumed.term, colour = "Consumed"), size = 0.7) +
    scale_colour_manual("",
                       values = c("Reprostorage" = "darkmagenta",
                                  "Gut" = "gold4",
                                  "Protein" = "brown",
                                  "Fat" = "burlywood3",
                                  "Excess" = "darkorange",
                                  "Shortterm" = "goldenrod2",
                                  "Ingested" = "darkgreen",
                                  "Net" = "darkblue",
                                  "Consumed" = "darkred")) +
    scale_fill_manual("",
                      values = c("Reprostorage" = "darkmagenta",
                                  "Gut" = "gold4",
                                  "Protein" = "brown",
                                  "Fat" = "burlywood3",
                                  "Excess" = "darkorange",
                                  "Shortterm" = "goldenrod2",
                                  "Ingested" = "green",
                                  "Net" = "blue",
                                  "Consumed" = "red")) +
                                  ylab("kJ") +
                                  xlab("Days")
#print(last_plot())

ggsave(filename = "AverageEnergyTerm.png", plot = last_plot(), path = saveDir, units = "cm", width = 25, height = 15, dpi = 200)

agePlot <- ggplot(data = data_averages, aes(x = days)) +
    geom_ribbon(aes(ymin = mean_age.juv - z * (mean_age.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    ymax = mean_age.juv + z * (mean_age.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    fill = "juvenile"), alpha = 0.5, show.legend = F) +
                    geom_point(aes(y = mean_age.juv, colour = "juvenile"), size = 0.7) +
                    geom_line(aes(y = mean_age.juv, colour = "juvenile"), size = 0.4) +
                    geom_ribbon(aes(ymin = mean_age.init - z * (mean_age.initSD / sqrt(data_averages$initDAYS$Day)),
                    ymax = mean_age.init + z * (mean_age.initSD / sqrt(data_averages$initDAYS$Day)),
                    fill = "initial"), alpha = 0.5, show.legend = F) +
                    geom_point(aes(y = mean_age.init, colour = "initial"), size = 0.7) +
                    geom_line(aes(y = mean_age.init, colour = "initial"), size = 0.4) +
                    geom_ribbon(aes(ymin = mean_age.term - z * (mean_age.termSD / sqrt(data_averages$termDAYS$Day)),
                    ymax = mean_age.term + z * (mean_age.termSD / sqrt(data_averages$termDAYS$Day)),
                    fill = "terminal"), alpha = 0.5, show.legend = F) +
    geom_point(aes(y = mean_age.term, colour = "terminal"), size = 0.7) +
    geom_line(aes(y = mean_age.term, colour = "terminal"), size = 0.4) +
    scale_colour_manual("", values = c("initial" = "red", "juvenile" = "blue", "terminal" = "green")) +
    scale_fill_manual("", values = c("initial" = "red", "juvenile" = "blue", "terminal" = "green")) +
                       ylab("Years") +
                       xlab("Days")
#print(last_plot())
ggsave(filename = "AverageAge.png", plot = last_plot(), path = saveDir, units = "cm", width = 25, height = 15, dpi = 200)

lengthPlot <- ggplot(data = data_averages, aes(x = days)) +
    geom_ribbon(aes(ymin = mean_length.juv - z * (mean_length.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    ymax = mean_length.juv + z * (mean_length.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    fill = "juvenile"), alpha = 0.5, show.legend = F) +
                    geom_point(aes(y = mean_length.juv, colour = "juvenile"), size = 0.7) +
                    geom_line(aes(y = mean_length.juv, colour = "juvenile"), size = 0.4) +
                    geom_ribbon(aes(ymin = mean_length.init - z * (mean_length.initSD / sqrt(data_averages$initDAYS$Day)),
                    ymax = mean_length.init + z * (mean_length.initSD / sqrt(data_averages$initDAYS$Day)),
                    fill = "initial"), alpha = 0.5, show.legend = F) +
                    geom_point(aes(y = mean_length.init, colour = "initial"), size = 0.7) +
                    geom_line(aes(y = mean_length.init, colour = "initial"), size = 0.4) +
                    geom_ribbon(aes(ymin = mean_length.term - z * (mean_length.termSD / sqrt(data_averages$termDAYS$Day)),
                    ymax = mean_length.term + z * (mean_length.termSD / sqrt(data_averages$termDAYS$Day)),
                    fill = "terminal"), alpha = 0.5, show.legend = F) +
    geom_point(aes(y = mean_length.term, colour = "terminal"), size = 0.7) +
    geom_line(aes(y = mean_length.term, colour = "terminal"), size = 0.4) +
    scale_colour_manual("", values = c("initial" = "red", "juvenile" = "blue", "terminal" = "green")) +
    scale_fill_manual("", values = c("initial" = "red", "juvenile" = "blue", "terminal" = "green")) +
                       ylab("cm") +
                       xlab("Days")

#print(last_plot())
ggsave(filename = "AverageLength.png", plot = last_plot(), path = saveDir, units = "cm", width = 25, height = 15, dpi = 200)

deathPlot <- ggplot(data_dead, aes(x = data_dead$Cause_of_Death, fill = data_dead$Phase)) +
       geom_bar() +
      scale_fill_manual("Phase",
                       values = c("INITIAL" = "#FF4040", "JUVENILE" = "#4141FF", "TERMINAL" = "#2DD02D", "DEAD" = "black"))

#print(last_plot())
ggsave(filename = "DeathCauses.png", plot = last_plot(), path = saveDir, units = "cm", width = 12, height = 20, dpi = 200)

populationPlot <- ggplot(data = data_averages, aes(x = days)) +
    geom_ribbon(aes(ymin = population.juv - z * (population.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    ymax = population.juv + z * (population.juvSD / sqrt(data_averages$juvDAYS$Day)),
                    fill = "juvenile"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = population.juv, colour = "juvenile"), size = 0.4) +
                    geom_point(aes(y = population.juv, colour = "juvenile"), size = 0.7) +
                    geom_ribbon(aes(ymin = population.init - z * (population.initSD / sqrt(data_averages$initDAYS$Day)),
                    ymax = population.init + z * (population.initSD / sqrt(data_averages$initDAYS$Day)),
                    fill = "initial"), alpha = 0.5, show.legend = F) +
                    geom_line(aes(y = population.init, colour = "initial"), size = 0.4) +
                    geom_point(aes(y = population.init, colour = "initial"), size = 0.7) +
                    geom_ribbon(aes(ymin = population.term - z * (population.termSD / sqrt(data_averages$termDAYS$Day)),
                    ymax = population.term + z * (population.termSD / sqrt(data_averages$termDAYS$Day)),
                    fill = "terminal"), alpha = 0.5, show.legend = F) +
    geom_line(aes(y = population.term, colour = "terminal"), size = 0.4) +
    geom_point(aes(y = population.term, colour = "terminal"), size = 0.7) +
    scale_colour_manual("", values = c("initial" = "red", "juvenile" = "blue", "terminal" = "green")) +
    scale_fill_manual("", values = c("initial" = "red", "juvenile" = "blue", "terminal" = "green")) +
                       ylab("Count") +
                       xlab("Days")

#print(last_plot())
ggsave(filename = "Population.png", plot = last_plot(), path = saveDir, units = "cm", width = 30, height = 15, dpi = 200)