import asyncio
import sys
import unicodedata
import json

# Force UTF-8 encoding for stdout and stderr
if sys.stdout.encoding != 'UTF-8':
    sys.stdout.reconfigure(encoding='UTF-8')
if sys.stderr.encoding != 'UTF-8':
    sys.stderr.reconfigure(encoding='UTF-8')

def get_field_context(field_name):
    """Get specific context and examples for each field"""
    field_contexts = {
        # Section 1 - Dossier creation
        "codesociete": {
            "context": "identification légale de l'entreprise",
            "examples": "SIREN (9 chiffres) comme 123456789 ou SIRET (14 chiffres) comme 12345678901234",
            "tone": "professionnel et précis"
        },
        "matriculesalarie": {
            "context": "identifiant unique du salarié dans l'entreprise",
            "examples": "numéro attribué par les RH, souvent alphanumérique comme QAAZE001 ou QAFAE005",
            "tone": "administratif et clair"
        },

        # Section 2 - Individual info
        "nomusuel": {
            "context": "nom utilisé au quotidien, peut différer du nom patronymique",
            "examples": "nom d'épouse ou nom d'usage choisi",
            "tone": "bienveillant et respectueux"
        },
        "nompatronymique": {
            "context": "nom de famille officiel inscrit à l'état civil",
            "examples": "nom de naissance, tel qu'il apparaît sur la carte d'identité",
            "tone": "officiel mais accessible"
        },
        "prenom": {
            "context": "prénom principal utilisé professionnellement",
            "examples": "le prénom usuel, tel qu'il apparaît sur les documents officiels",
            "tone": "chaleureux et personnel"
        },
        "deuxiemeprenom": {
            "context": "prénom secondaire optionnel",
            "examples": "deuxième ou troisième prénom si souhaité pour l'usage professionnel",
            "tone": "optionnel et flexible"
        },
        "numeroinsee": {
            "context": "numéro de sécurité sociale français",
            "examples": "13 chiffres + 2 chiffres de clé, format: 1 85 02 75 116 001 23",
            "tone": "confidentiel et sécurisé"
        },
        "villenaissance": {
            "context": "commune de naissance selon l'état civil",
            "examples": "nom officiel de la ville ou commune de naissance",
            "tone": "personnel et précis"
        },

        # Section 3 - Addresses
        "complement1": {
            "context": "précision d'adresse niveau 1",
            "examples": "bâtiment, résidence, étage comme 'Résidence Les Jardins' ou 'Bât. A, 3ème étage'",
            "tone": "pratique et détaillé"
        },
        "complement2": {
            "context": "précision d'adresse niveau 2",
            "examples": "appartement, boîte aux lettres comme 'Apt 15' ou 'Porte droite'",
            "tone": "pratique et détaillé"
        },
        "lieudit": {
            "context": "nom de lieu-dit ou hameau",
            "examples": "pour les adresses rurales, nom du hameau ou lieu-dit comme 'Les Trois Chênes'",
            "tone": "géographique et local"
        },
        "codepostal": {
            "context": "code postal français à 5 chiffres",
            "examples": "format 75001 pour Paris 1er, 13001 pour Marseille 1er",
            "tone": "technique et précis"
        },
        "commune": {
            "context": "nom officiel de la commune de résidence",
            "examples": "nom exact de la ville ou commune selon l'INSEE",
            "tone": "administratif et géographique"
        },
        "codeinseecommune": {
            "context": "code INSEE officiel de la commune",
            "examples": "code à 5 chiffres attribué par l'INSEE, comme 75101 pour Paris 1er",
            "tone": "technique et administratif"
        },
        "numerovoie": {
            "context": "numéro dans la voie de résidence",
            "examples": "numéro de rue, avenue, boulevard comme '15' ou '23 bis'",
            "tone": "pratique et précis"
        },

        # Section 4 - Assignments
        "poste": {
            "context": "intitulé du poste occupé",
            "examples": "fonction précise comme 'Développeur Full Stack' ou 'Assistante RH'",
            "tone": "professionnel et valorisant"
        },
        "emploi": {
            "context": "catégorie d'emploi ou classification",
            "examples": "niveau hiérarchique ou famille d'emploi comme 'Cadre' ou 'Technicien niveau II'",
            "tone": "professionnel et structuré"
        },
        "uniteorganisationnelle": {
            "context": "service ou département d'affectation",
            "examples": "division, service, équipe comme 'Direction IT' ou 'Service Commercial Nord'",
            "tone": "organisationnel et précis"
        },
        "codecycle": {
            "context": "code du cycle de travail appliqué",
            "examples": "référence du planning comme 'CYC001' ou 'STAND35H'",
            "tone": "technique et planification"
        },

        # Section 5 - Career
        "accordentreprise": {
            "context": "référence de l'accord d'entreprise applicable",
            "examples": "accord collectif spécifique comme 'Accord télétravail 2024' ou 'AE-RTT-2023'",
            "tone": "juridique et contractuel"
        },
        "duree": {
            "context": "durée du contrat de travail",
            "examples": "période comme '12 mois', '2 ans' ou 'indéterminée'",
            "tone": "contractuel et temporel"
        },
        "modalitehoraire": {
            "context": "organisation des horaires de travail",
            "examples": "type d'horaire comme 'Fixe 9h-17h', 'Variables' ou 'Forfait jour'",
            "tone": "organisationnel et flexible"
        },
        "forfaitjours": {
            "context": "nombre de jours travaillés annuellement pour les cadres",
            "examples": "nombre entre 200 et 218 jours selon la convention",
            "tone": "contractuel et quantitatif"
        },
        "forfaitheures": {
            "context": "volume d'heures annuel contractuel",
            "examples": "nombre d'heures comme '1607' (temps plein légal) ou quotité réduite",
            "tone": "contractuel et quantitatif"
        },
        "heurestravaillees": {
            "context": "nombre d'heures effectivement travaillées",
            "examples": "volume horaire réel comme '35h', '28h' ou '39h'",
            "tone": "pratique et factuel"
        },
        "heurespayees": {
            "context": "nombre d'heures rémunérées",
            "examples": "peut différer des heures travaillées, format '35.0' ou '39.5'",
            "tone": "contractuel et financier"
        }
    }

    # Normalisation de la clé
    normalized_key = field_name.lower().replace(" ", "").replace("_", "").replace("-", "")
    return field_contexts.get(normalized_key, {
        "context": "information requise pour le dossier d'embauche",
        "examples": "selon les besoins de votre entreprise",
        "tone": "professionnel et utile"
    })

async def explain_field(field_name):
    """Generate explanation for a field using predefined contexts with a 1-second delay"""
    try:
        # Normalize input field name to ensure proper encoding
        field_name = unicodedata.normalize('NFC', field_name)
        print(f"Generating explanation for field: {field_name}", file=sys.stderr)

        # Add 1-second delay
        await asyncio.sleep(1)

        # Get explanation
        explanation = get_enhanced_fallback_explanation(field_name)

        # Normalize output to ensure consistent Unicode encoding
        explanation = unicodedata.normalize('NFC', explanation)

        print(f"Generated explanation: {explanation}", file=sys.stderr)
        return explanation

    except Exception as e:
        print(f"Error generating explanation: {str(e)}", file=sys.stderr)
        return unicodedata.normalize('NFC', f"Complétez le champ '{field_name}' avec les informations appropriées.")

def get_enhanced_fallback_explanation(field_name):
    """Enhanced fallback with context-aware responses"""
    field_lower = field_name.lower().strip()

    # Smart fallback based on field context
    enhanced_fallbacks = {
        # Section 1 - Dossier
        "codesociete": "Saisissez le code d'identification légale de votre entreprise, tel que le numéro SIREN (9 chiffres, ex: 123456789) ou le SIRET (14 chiffres, ex: 12345678901234). Ce code est disponible sur les documents officiels de l'entreprise, comme le Kbis.",
        "matriculesalarie": "Indiquez le numéro d'identification unique attribué à ce salarié par votre service des ressources humaines. Ce numéro est généralement au format alphanumérique, par exemple QAAZE001 ou QAFAE005. Vérifiez auprès de votre service RH si vous ne connaissez pas ce numéro.",

        # Section 2 - Identity
        "nomusuel": "Saisissez le nom utilisé au quotidien par la personne, qui peut différer du nom patronymique. Par exemple, une personne peut utiliser son nom d'épouse (ex: Marie Dupont au lieu de Marie Martin) ou un nom d'usage choisi pour des raisons personnelles ou professionnelles.",
        "nompatronymique": "Indiquez le nom de famille officiel tel qu'il figure sur les documents d'état civil, comme la carte d'identité ou le passeport. Par exemple, si une personne utilise un nom d'épouse, entrez ici son nom de naissance (ex: Martin pour Marie Martin).",
        "prenom": "Saisissez le prénom principal de la personne, tel qu'elle souhaite être appelée dans un cadre professionnel. Par exemple, si elle s'appelle Marie-Claire mais préfère être appelée Marie, indiquez 'Marie'. Ce prénom doit correspondre aux documents officiels.",
        "deuxiemeprenom": "Indiquez un prénom secondaire si la personne en utilise un dans le cadre professionnel. Ce champ est optionnel. Par exemple, pour une personne nommée Jean-Pierre-Paul Dupont, vous pourriez indiquer 'Pierre' comme deuxième prénom si pertinent.",
        "numeroinsee": "Saisissez le numéro de sécurité sociale français, composé de 13 chiffres suivis de 2 chiffres de clé (ex: 1 85 02 75 116 001 23). Ce numéro est confidentiel et doit être vérifié sur la carte Vitale ou un document officiel de la Sécurité sociale.",
        "villenaissance": "Indiquez la ville ou commune de naissance telle qu'elle apparaît sur les documents d'état civil. Par exemple, entrez 'Lyon' ou 'Saint-Émilion'. Si la personne est née à l'étranger, précisez la ville étrangère (ex: Casablanca).",

        # Section 3 - Address
        "complement1": "Précisez des informations complémentaires pour localiser l'adresse, comme le nom de la résidence, du bâtiment ou l'étage. Par exemple, 'Résidence Les Jardins', 'Bâtiment A, 3ème étage' ou 'Villa Les Pins'.",
        "complement2": "Ajoutez des précisions supplémentaires pour l'adresse, comme le numéro d'appartement, la porte ou la boîte aux lettres. Par exemple, 'Appartement 15', 'Porte droite' ou 'Boîte 4A'. Ce champ aide à identifier précisément le logement.",
        "lieudit": "Pour les adresses rurales, indiquez le nom du hameau ou du lieu-dit, si applicable. Par exemple, 'Les Trois Chênes' ou 'Le Moulin'. Ce champ est souvent utilisé pour les zones non urbaines où les adresses ne suivent pas un format de rue standard.",
        "codepostal": "Saisissez le code postal français à 5 chiffres correspondant à la commune de résidence. Par exemple, '75001' pour Paris 1er arrondissement ou '13001' pour Marseille 1er. Vérifiez le code exact sur les documents officiels ou le site de La Poste.",
        "commune": "Indiquez le nom exact de la ville ou commune de résidence selon les données officielles de l'INSEE. Par exemple, 'Marseille' ou 'Issy-les-Moulineaux'. Évitez les abréviations ou noms approximatifs pour garantir la précision.",
        "codeinseecommune": "Saisissez le code INSEE officiel de la commune, un code à 5 chiffres attribué par l'INSEE. Par exemple, '75101' pour Paris 1er arrondissement ou '13055' pour Marseille. Ce code est disponible sur les bases de données INSEE ou les documents administratifs.",
        "numerovoie": "Indiquez le numéro dans la voie de résidence, comme le numéro de la rue, de l'avenue ou du boulevard. Par exemple, '15' pour 15 Rue de la Paix ou '23 bis' pour 23 bis Avenue des Champs-Élysées. Incluez les suffixes comme 'bis' ou 'ter' si nécessaires.",

        # Section 4 - Assignment
        "poste": "Précisez l'intitulé exact du poste que la personne occupera dans l'entreprise. Par exemple, 'Développeur Full Stack', 'Assistante RH' ou 'Responsable Marketing Digital'. Utilisez le titre officiel mentionné dans le contrat de travail.",
        "emploi": "Indiquez la catégorie ou classification d'emploi selon la convention collective ou la structure interne. Par exemple, 'Cadre', 'Technicien niveau II', 'Agent de maîtrise' ou 'Employé qualifié'. Ce champ reflète le niveau hiérarchique ou la famille de métiers.",
        "uniteorganisationnelle": "Précisez le service, département ou équipe auquel la personne est rattachée. Par exemple, 'Direction des Technologies de l'Information', 'Service Commercial Nord' ou 'Équipe Logistique Paris'. Consultez l'organigramme de votre entreprise pour plus de précision.",
        "codecycle": "Saisissez le code de référence du cycle de travail ou du planning appliqué au salarié. Par exemple, 'CYC001' pour un cycle standard de 35 heures ou 'STAND35H' pour un horaire hebdomadaire fixe. Ce code est défini par votre service RH ou votre logiciel de gestion.",

        # Section 5 - Career
        "accordentreprise": "Indiquez la référence de l'accord d'entreprise applicable au poste, s'il existe. Par exemple, 'Accord télétravail 2024', 'AE-RTT-2023' ou 'Accord sur les horaires variables'. Consultez les documents RH ou juridiques de votre entreprise pour identifier l'accord pertinent.",
        "duree": "Précisez la durée du contrat de travail. Par exemple, '12 mois' pour un CDD d'un an, '2 ans' pour un contrat temporaire, ou 'indéterminée' pour un CDI. Ce champ reflète la période contractuelle convenue dans l'offre d'emploi.",
        "modalitehoraire": "Décrivez l'organisation des horaires de travail. Par exemple, 'Horaires fixes 9h-17h' pour un emploi de bureau standard, 'Variables' pour des horaires changeants, ou 'Forfait jour' pour les cadres. Précisez si des aménagements spécifiques s'appliquent.",
        "forfaitjours": "Indiquez le nombre de jours travaillés par an pour les cadres soumis au forfait jours, généralement entre 200 et 218 jours selon la convention collective. Par exemple, '218 jours' pour un forfait standard ou '205 jours' si des jours de repos supplémentaires s'appliquent.",
        "forfaitheures": "Saisissez le volume annuel d'heures contractuel, basé sur la convention ou le contrat. Par exemple, '1607 heures' pour un temps plein légal en France ou '1286 heures' pour un temps partiel à 80%. Vérifiez les termes du contrat pour ce calcul.",
        "heurestravaillees": "Indiquez le nombre d'heures hebdomadaires effectivement travaillées par le salarié. Par exemple, '35h' pour un temps plein standard, '28h' pour un temps partiel, ou '39h' si des heures supplémentaires sont prévues. Ce champ reflète le temps réel de travail.",
        "heurespayees": "Précisez le nombre d'heures rémunérées par semaine, qui peut inclure des heures supplémentaires ou différer des heures travaillées en raison d'absences payées. Par exemple, '35.0' pour un temps plein ou '39.5' si des heures supplémentaires sont incluses."
    }

    # Clean field name for lookup
    clean_field = field_lower.replace(" ", "").replace("_", "").replace("-", "")

    # Find exact match
    if clean_field in enhanced_fallbacks:
        return enhanced_fallbacks[clean_field]

    # Find partial matches
    for key, value in enhanced_fallbacks.items():
        if key in clean_field or clean_field in key:
            return value

    # Pattern-based fallbacks
    if any(word in field_lower for word in ["code", "numéro", "numero", "matricule"]):
        return f"Saisissez le code ou identifiant requis pour '{field_name}'. Consultez votre documentation interne, comme les registres RH ou les bases de données administratives, pour obtenir la valeur correcte."
    elif "date" in field_lower:
        return "Sélectionnez une date au format JJ/MM/AAAA à l'aide du calendrier. Assurez-vous que la date est valide et cohérente avec les informations demandées (ex: date de naissance ou début de contrat)."
    elif any(word in field_lower for word in ["nom", "name", "prénom", "prenom"]):
        return f"Saisissez {field_name.lower()} tel qu'il doit apparaître dans les documents officiels. Vérifiez les documents d'identité ou d'état civil pour garantir l'exactitude."
    elif any(word in field_lower for word in ["adresse", "rue", "avenue", "voie"]):
        return f"Complétez cette information d'adresse pour {field_name.lower()}. Utilisez des informations précises tirées de documents officiels ou de correspondances récentes."
    elif any(word in field_lower for word in ["heure", "temps", "durée", "duree"]):
        return f"Indiquez la valeur numérique appropriée pour {field_name.lower()}. Par exemple, utilisez des formats comme '35' pour les heures ou '12 mois' pour les durées, selon les termes du contrat."

    # Default fallback
    return f"Complétez le champ '{field_name}' avec les informations appropriées selon votre situation. Consultez les documents RH ou administratifs pertinents pour plus de détails."

async def main():
    if len(sys.argv) != 2:
        print("Usage: python explain_field.py '<field_name>'", file=sys.stderr)
        sys.exit(1)

    field_name = sys.argv[1]
    explanation = await explain_field(field_name)

    # Output as JSON to ensure proper encoding
    output =  explanation
    print(json.dumps(output, ensure_ascii=False, indent=None))

if __name__ == "__main__":
    import platform
    if platform.system() == "Emscripten":
        asyncio.ensure_future(main())
    else:
        asyncio.run(main())