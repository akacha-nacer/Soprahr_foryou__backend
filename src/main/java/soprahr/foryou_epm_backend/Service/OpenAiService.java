package soprahr.foryou_epm_backend.Service;

import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;

@Service
public class OpenAiService {
    private static final Logger LOGGER = Logger.getLogger(OpenAiService.class.getName());
    private static final int TIMEOUT_SECONDS = 120; // Timeout for script execution

    public String getFieldExplanation(String fieldName) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            return "Nom du champ non spécifié.";
        }

        String cleanFieldName = fieldName.trim();
        LOGGER.info("Getting explanation for field: " + cleanFieldName);

        try {
            String scriptPath = getScriptPath();
            LOGGER.info("Using script path: " + scriptPath);

            // Determine Python command based on OS
            String pythonCommand = getPythonCommand();
            LOGGER.info("Using Python command: " + pythonCommand);

            // Create process to run Python script
            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonCommand, scriptPath, cleanFieldName
            );

            // Set working directory to project root
            processBuilder.directory(new File(System.getProperty("user.dir")));

            // Start the process
            Process process = processBuilder.start();

            // Use CompletableFuture for timeout handling
            CompletableFuture<String> futureResult = CompletableFuture.supplyAsync(() -> {
                try {
                    StringBuilder result = new StringBuilder();
                    StringBuilder errors = new StringBuilder();

                    // Read stdout
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line).append("\n");
                        }
                    }

                    // Read stderr (for debugging info)
                    try (BufferedReader errorReader = new BufferedReader(
                            new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            errors.append(line).append("\n");
                        }
                    }

                    // Log stderr messages (they're not necessarily errors, could be info)
                    String errorOutput = errors.toString().trim();
                    if (!errorOutput.isEmpty()) {
                        LOGGER.info("Python script info/debug output: " + errorOutput);
                    }

                    return result.toString().trim();

                } catch (IOException e) {
                    LOGGER.severe("IOException reading process output: " + e.getMessage());
                    return null;
                }
            });

            // Wait for process to complete with timeout
            String result;
            try {
                result = futureResult.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (Exception e) {
                process.destroyForcibly();
                LOGGER.warning("Python script timed out after " + TIMEOUT_SECONDS + " seconds for field: " + cleanFieldName);
                return getFallbackExplanation(cleanFieldName);
            }

            // Wait for process to finish and check exit code
            try {
                int exitCode = process.waitFor(5, TimeUnit.SECONDS) ? process.exitValue() : -1;

                if (exitCode != 0) {
                    LOGGER.warning("Python script failed with exit code " + exitCode + " for field: " + cleanFieldName);
                    return getFallbackExplanation(cleanFieldName);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return getFallbackExplanation(cleanFieldName);
            }

            // Process the result
            if (result == null || result.isEmpty()) {
                LOGGER.warning("Empty response from Python script for field: " + cleanFieldName);
                return getFallbackExplanation(cleanFieldName);
            }

            String explanation = result.trim();
            LOGGER.info("Successfully generated explanation for field '" + cleanFieldName + "': " + explanation);

            return explanation;

        } catch (Exception e) {
            LOGGER.severe("Unexpected error for field '" + cleanFieldName + "': " + e.getMessage());
            return getFallbackExplanation(cleanFieldName);
        }
    }

    private String getPythonCommand() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            // Try 'py' first (Python Launcher), then fallback to full path
            return "py";
        } else {
            return "python3";
        }
    }

    private String getScriptPath() throws IOException {
        try {
            // Try to get from classpath first (for JAR deployment)
            File scriptFile = ResourceUtils.getFile("classpath:scripts/explain_fieldd.py");
            return scriptFile.getAbsolutePath();
        } catch (Exception e) {
            // Fallback to relative path (for development)
            String relativePath = "src/main/resources/scripts/explain_fieldd.py";
            File scriptFile = new File(relativePath);
            if (scriptFile.exists()) {
                return scriptFile.getAbsolutePath();
            } else {
                throw new IOException("Python script not found at: " + relativePath);
            }
        }
    }

    private String getFallbackExplanation(String fieldName) {
        // Align with Python script's enhanced fallback explanations
        String lowerFieldName = fieldName.toLowerCase().trim();
        String cleanField = lowerFieldName.replace(" ", "").replace("_", "").replace("-", "");

        // Exact match fallbacks (aligned with Python script)
        switch (cleanField) {
            case "codesociete":
                return "Saisissez le code d'identification de votre entreprise (numéro SIREN à 9 chiffres ou SIRET à 14 chiffres).";
            case "matriculesalarie":
                return "Indiquez le numéro d'identification unique attribué à ce salarié par votre service RH (format alphanumérique comme QAAZE001).";
            case "nomusuel":
                return "Saisissez le nom utilisé au quotidien par la personne (peut différer du nom patronymique, ex: nom d'épouse).";
            case "nompatronymique":
                return "Indiquez le nom de famille officiel tel qu'il apparaît sur les documents d'identité.";
            case "prenom":
                return "Saisissez le prénom principal de la personne, tel qu'elle souhaite être appelée professionnellement.";
            case "deuxiemeprenom":
                return "Indiquez un prénom secondaire si la personne en utilise un dans le cadre professionnel (optionnel).";
            case "numeroinsee":
                return "Saisissez le numéro de sécurité sociale (13 chiffres + 2 chiffres de clé). Ces informations restent confidentielles.";
            case "villenaissance":
                return "Indiquez la ville ou commune de naissance selon l'état civil.";
            case "complement1":
                return "Précisez des informations complémentaires comme le nom de résidence, bâtiment ou étage.";
            case "complement2":
                return "Ajoutez d'autres précisions d'adresse comme le numéro d'appartement ou porte.";
            case "lieudit":
                return "Pour les adresses rurales, indiquez le nom du hameau ou lieu-dit si applicable.";
            case "codepostal":
                return "Saisissez le code postal à 5 chiffres de la commune de résidence.";
            case "commune":
                return "Indiquez le nom exact de la ville ou commune selon les données INSEE.";
            case "codeinseecommune":
                return "Saisissez le code officiel INSEE de la commune (5 chiffres).";
            case "numerovoie":
                return "Indiquez le numéro dans la rue, avenue ou boulevard (ex: 15, 23 bis).";
            case "poste":
                return "Précisez l'intitulé exact du poste que la personne va occuper dans votre entreprise.";
            case "emploi":
                return "Indiquez la catégorie ou classification d'emploi (ex: Cadre, Technicien, Agent de maîtrise).";
            case "uniteorganisationnelle":
                return "Précisez le service, département ou équipe de rattachement.";
            case "codecycle":
                return "Saisissez le code de référence du cycle de travail ou planning appliqué.";
            case "accordentreprise":
                return "Indiquez la référence de l'accord d'entreprise applicable au poste si existant.";
            case "duree":
                return "Précisez la durée du contrat (ex: 12 mois, 2 ans, ou indéterminée pour un CDI).";
            case "modalitehoraire":
                return "Décrivez l'organisation des horaires (ex: Horaires fixes 9h-17h, Variables, Forfait jour).";
            case "forfaitjours":
                return "Indiquez le nombre de jours travaillés par an pour les cadres (généralement entre 200 et 218 jours).";
            case "forfaitheures":
                return "Saisissez le volume annuel d'heures contractuel (ex: 1607h pour un temps plein).";
            case "heurestravaillees":
                return "Indiquez le nombre d'heures hebdomadaires effectivement travaillées.";
            case "heurespayees":
                return "Précisez le nombre d'heures rémunérées (peut différer des heures travaillées).";
        }

        // Pattern-based fallbacks (aligned with Python script)
        if (lowerFieldName.contains("code") || lowerFieldName.contains("numéro") || lowerFieldName.contains("numero") || lowerFieldName.contains("matricule")) {
            return "Saisissez le code ou identifiant requis pour '" + fieldName + "'. Consultez votre documentation interne si nécessaire.";
        } else if (lowerFieldName.contains("date")) {
            return "Sélectionnez une date au format JJ/MM/AAAA à l'aide du calendrier.";
        } else if (lowerFieldName.contains("nom") || lowerFieldName.contains("name") || lowerFieldName.contains("prénom") || lowerFieldName.contains("prenom")) {
            return "Saisissez " + lowerFieldName + " tel qu'il doit apparaître dans les documents officiels.";
        } else if (lowerFieldName.contains("adresse") || lowerFieldName.contains("rue") || lowerFieldName.contains("avenue") || lowerFieldName.contains("voie")) {
            return "Complétez cette information d'adresse pour " + lowerFieldName + ".";
        } else if (lowerFieldName.contains("heure") || lowerFieldName.contains("temps") || lowerFieldName.contains("durée") || lowerFieldName.contains("duree")) {
            return "Indiquez la valeur numérique appropriée pour " + lowerFieldName + ".";
        }

        // Default fallback
        return "Complétez le champ '" + fieldName + "' avec les informations appropriées selon votre situation.";
    }
}