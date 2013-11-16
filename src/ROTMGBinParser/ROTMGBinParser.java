package ROTMGBinParser;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
/*
	Author: zekikez
	Purpose: Parse ROTMG Bins to extract object data.
 */
public class ROTMGBinParser {

	/**
	 * main
	 * @param args no arguments needed
	 */
	public static void main(String[] args) {
		// find all files with ".bin" extension
		try {
			final Parser parser = new Parser();
			Path startPath = Paths.get("");
			ROTMGBinParser.print("Parsing bins...");
			Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					if (file.toString().endsWith(".bin"))
					{
						if (!parser.ParseBin(file))
							ROTMGBinParser.print("Failed to parse: " + file.toAbsolutePath());
					}

					return FileVisitResult.CONTINUE;
				}
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
			ROTMGBinParser.print("Generating XMLs...");
			parser.generateXml();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void print(String message)
	{
		System.out.println(message);
	}
}
