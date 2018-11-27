library(readr)
library(ggplot2)
dataSet <- read_csv("SimpleLinearTranslator583430083507717.txt", col_names = FALSE, skip = 1)
colnames(dataSet) <- c("Time", "Position", "PressureExt", "PressureRet")
trimmedData <- dataSet

p <- ggplot(trimmedData, aes(x = trimmedData$Time))
minPosition = min(trimmedData$Position)
maxPosition = max(trimmedData$Position)
minPressureExt = min(trimmedData$PressureExt)
maxPressureExt = max(trimmedData$PressureExt)

transformedPressureExt = (trimmedData$PressureExt - minPressureExt) * (maxPosition - minPosition) / (maxPressureExt - minPressureExt) + minPosition
transformedPressureRet = (trimmedData$PressureRet - minPressureExt) * (maxPosition - minPosition) / (maxPressureExt - minPressureExt) + minPosition

p <- p + geom_line(aes(y = trimmedData$Position, color = "Position"))
p <- p + geom_line(aes(y = transformedPressureExt, color = "Extension Pressure"))
p <- p + geom_line(aes(y = transformedPressureRet, color = "Retraction Pressure"))

p <- p + scale_y_continuous(sec.axis = sec_axis(~.*(maxPressureExt-minPressureExt)/(maxPosition - minPosition) - minPosition * (maxPressureExt-minPressureExt)/(maxPosition - minPosition) + minPressureExt, name = "Pressure (PSI)"))

p <- p + scale_color_manual(values = c("blue", "red", "green"))
p <- p + labs(y = "Position (in)", x = "Time", color = "Legend")
p <- p + theme(legend.position = c(0.8,0.3))
show(p)
