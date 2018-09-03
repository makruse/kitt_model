src <- "run_00004/" #the source directory
fileName <- "stayWithoutUnits.csv"
saveFolder <- "stayDurations"
savePath <- paste(src, saveFolder, sep = "")
filePath <- paste(src, fileName, sep = "")
data <- read.csv(filePath)
data$stay_duration_total <- as.integer(as.character(data$stay_duration_total)) #as.character before converting otherwise weird stuff happens
data$stay_duration_night <- as.integer(as.character(data$stay_duration_night))
data$stay_duration_day <- as.integer(as.character(data$stay_duration_day))

dir.create(file.path(src, saveFolder))
library(ggplot2)
for (i in as.numeric(levels(as.factor(data$days)))) {
    data2 <- data[which(data$days == i),]

    ggplot(data2, aes(data2$cell_x, rev(data2$cell_y))) + #rev -> revert order of y values; for correct map orientation
       geom_tile(aes(fill = data2$stay_duration_total)) +
       scale_fill_gradient(name = "Stay Duration", low = "white", high = "steelblue") +
       xlab("") +
       ylab("")

    ggsave(filename = paste0(paste0("stayDurationsTotal_day_",i),".png"), plot = last_plot(), path = savePath, units = "cm", width = 14, height = 16, dpi = 200)
}