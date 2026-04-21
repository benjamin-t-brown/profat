package net.revirtualis.profat.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class StaticSiteTryFilesTest {

	@Test
	void extensionlessPath_resolvesSiblingHtml(@TempDir Path root) throws IOException {
		Path dir = root.resolve("game-posts");
		Files.createDirectories(dir);
		Path page = dir.resolve("regem-ludos.html");
		Files.writeString(page, "<html>x</html>", StandardCharsets.UTF_8);

		Path resolved = StaticSiteController.tryFilesResolve(root, "game-posts/regem-ludos");
		assertThat(resolved).isEqualTo(page.normalize());
	}

	@Test
	void directoryWithIndex_prefersIndexOverHtml(@TempDir Path root) throws IOException {
		Path dir = root.resolve("section");
		Files.createDirectories(dir);
		Files.writeString(dir.resolve("index.html"), "<html>idx</html>", StandardCharsets.UTF_8);
		Files.writeString(root.resolve("section.html"), "<html>sib</html>", StandardCharsets.UTF_8);

		Path resolved = StaticSiteController.tryFilesResolve(root, "section");
		assertThat(resolved).isEqualTo(dir.resolve("index.html").normalize());
	}

	@Test
	void emptyRemainder_usesRootIndex(@TempDir Path root) throws IOException {
		Files.writeString(root.resolve("index.html"), "<html/>", StandardCharsets.UTF_8);
		assertThat(StaticSiteController.tryFilesResolve(root, "")).isEqualTo(root.resolve("index.html").normalize());
		assertThat(StaticSiteController.tryFilesResolve(root, null)).isEqualTo(root.resolve("index.html").normalize());
	}
}
