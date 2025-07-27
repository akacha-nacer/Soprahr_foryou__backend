# src/main/resources/scripts/explain_field.py
# Fixed version with sequential downloads, multiple fallback options, and authentication handling

import sys
import os
import json
from transformers import pipeline, AutoTokenizer, AutoModelForCausalLM
import warnings
import torch

# Suppress warnings
warnings.filterwarnings("ignore")

def get_field_context(field_name):
    """Get specific context and examples for each field"""
    field_contexts = {
        # Section 1 - Dossier creation
        "codesociete": {
            "definition": "Code d'identification légale de l'entreprise",
            "format": "SIREN (9 chiffres) ou SIRET (14 chiffres)",
            "exemple": "123456789 ou 12345678901234"
        },
        "matriculesalarie": {
            "definition": "Identifiant unique du salarié dans l'entreprise",
            "format": "Code alphanumérique attribué par les RH",
            "exemple": "QAAZE001 ou QAAEE005"
        },
        "nomusuel": {
            "definition": "Nom utilisé au quotidien par la personne",
            "format": "Peut différer du nom patronymique",
            "exemple": "Nom d'épouse ou nom d'usage choisi"
        },
        "nompatronymique": {
            "definition": "Nom de famille officiel inscrit à l'état civil",
            "format": "Nom de naissance sur les documents d'identité",
            "exemple": "Tel qu'il apparaît sur la carte d'identité"
        },
        "prenom": {
            "definition": "Prénom principal utilisé professionnellement",
            "format": "Prénom usuel pour les documents officiels",
            "exemple": "Premier prénom d'usage"
        },
        "deuxiemeprenom": {
            "definition": "Prénom secondaire optionnel",
            "format": "Deuxième ou troisième prénom si utilisé",
            "exemple": "Marie-Claire → Marie (1er) Claire (2ème)"
        },
        "numeroinsee": {
            "definition": "Numéro de sécurité sociale français",
            "format": "13 chiffres + 2 chiffres de clé de contrôle",
            "exemple": "1 85 02 75 116 001 23"
        },
        "villenaissance": {
            "definition": "Commune de naissance selon l'état civil",
            "format": "Nom officiel de la ville de naissance",
            "exemple": "Paris, Lyon, Marseille..."
        },
        # Section 3 - Addresses
        "complement1": {
            "definition": "Complément d'adresse niveau 1",
            "format": "Bâtiment, résidence, étage",
            "exemple": "Résidence Les Jardins, Bât. A"
        },
        "complement2": {
            "definition": "Complément d'adresse niveau 2",
            "format": "Appartement, boîte aux lettres",
            "exemple": "Apt 15, Porte droite"
        },
        "lieudit": {
            "definition": "Nom de lieu-dit ou hameau",
            "format": "Pour les adresses rurales",
            "exemple": "Les Trois Chênes, Le Moulin"
        },
        "codepostal": {
            "definition": "Code postal français",
            "format": "5 chiffres",
            "exemple": "75001, 13001, 69001"
        },
        "commune": {
            "definition": "Nom officiel de la commune de résidence",
            "format": "Nom exact selon l'INSEE",
            "exemple": "Paris 1er Arrondissement"
        },
        "codeinseecommune": {
            "definition": "Code INSEE officiel de la commune",
            "format": "5 chiffres attribués par l'INSEE",
            "exemple": "75101 pour Paris 1er"
        },
        "numerovoie": {
            "definition": "Numéro dans la voie de résidence",
            "format": "Numéro de rue, avenue, boulevard",
            "exemple": "15, 23 bis, 147"
        },
        # Section 4 - Professional
        "poste": {
            "definition": "Intitulé du poste occupé",
            "format": "Fonction précise dans l'entreprise",
            "exemple": "Développeur Full Stack, Assistante RH"
        },
        "emploi": {
            "definition": "Catégorie d'emploi ou classification",
            "format": "Niveau hiérarchique ou famille d'emploi",
            "exemple": "Cadre, Technicien niveau II"
        },
        "uniteorganisationnelle": {
            "definition": "Service ou département d'affectation",
            "format": "Division, service, équipe",
            "exemple": "Direction IT, Service Commercial"
        },
        "codecycle": {
            "definition": "Code du cycle de travail appliqué",
            "format": "Référence du planning de travail",
            "exemple": "CYC001, STAND35H"
        },
        # Section 5 - Contract
        "accordentreprise": {
            "definition": "Référence de l'accord d'entreprise applicable",
            "format": "Accord collectif spécifique",
            "exemple": "Accord télétravail 2024"
        },
        "duree": {
            "definition": "Durée du contrat de travail",
            "format": "Période contractuelle",
            "exemple": "12 mois, 2 ans, indéterminée"
        },
        "modalitehoraire": {
            "definition": "Organisation des horaires de travail",
            "format": "Type d'horaire appliqué",
            "exemple": "Fixe 9h-17h, Variables, Forfait jour"
        },
        "forfaitjours": {
            "definition": "Nombre de jours travaillés annuellement",
            "format": "Pour les cadres en forfait jour",
            "exemple": "Entre 200 et 218 jours"
        },
        "forfaitheures": {
            "definition": "Volume d'heures annuel contractuel",
            "format": "Nombre d'heures selon le contrat",
            "exemple": "1607h (temps plein légal)"
        },
        "heurestravaillees": {
            "definition": "Nombre d'heures effectivement travaillées",
            "format": "Volume horaire hebdomadaire réel",
            "exemple": "35h, 28h, 39h"
        },
        "heurespayees": {
            "definition": "Nombre d'heures rémunérées",
            "format": "Peut différer des heures travaillées",
            "exemple": "35.0h, 39.5h"
        }
    }

    # Normalisation de la clé
    normalized_key = field_name.lower().replace(" ", "").replace("_", "").replace("-", "")

    return field_contexts.get(normalized_key, {
        "definition": "Information requise pour le dossier d'embauche",
        "format": "Selon les besoins de votre entreprise",
        "exemple": "Consultez votre documentation"
    })

def get_available_models():
    """Get list of available models (non-gated alternatives)"""
    return [
        "microsoft/DialoGPT-small",  # Free, no authentication needed
        "distilbert-base-uncased",   # Free, no authentication needed
        "t5-small",                  # Free, no authentication needed
        "google/flan-t5-small",      # Free, no authentication needed
        "microsoft/DialoGPT-medium", # Free, no authentication needed
    ]

def check_hf_authentication():
    """Check if Hugging Face authentication is available"""
    try:
        from huggingface_hub import HfApi
        api = HfApi()
        # Try to get user info to check authentication
        user_info = api.whoami()
        return True, user_info
    except Exception as e:
        return False, str(e)

def create_simple_prompt(field_name):
    """Create a simple prompt for any text generation model"""
    context = get_field_context(field_name)

    prompt = f"""Expliquez en une phrase claire ce qu'il faut saisir dans le champ "{field_name}".
Contexte: {context['definition']}
Format: {context['format']}
Exemple: {context['exemple']}

Réponse courte:"""

    return prompt

def try_alternative_model(field_name):
    """Try using an alternative free model"""
    try:
        print("🔄 Tentative avec un modèle alternatif...", file=sys.stderr)

        # Try T5 for text generation
        model_name = "google/flan-t5-small"

        print(f"📦 Chargement de {model_name}...", file=sys.stderr)

        # Create text generation pipeline
        generator = pipeline(
            "text2text-generation",
            model=model_name,
            tokenizer=model_name,
            device=0 if torch.cuda.is_available() else -1,
            torch_dtype=torch.float16 if torch.cuda.is_available() else torch.float32
        )

        prompt = create_simple_prompt(field_name)

        print("🚀 Génération avec modèle alternatif...", file=sys.stderr)

        # Generate response
        response = generator(
            prompt,
            max_length=100,
            min_length=10,
            temperature=0.3,
            do_sample=True,
            top_p=0.8
        )

        if response and len(response) > 0:
            explanation = response[0]['generated_text'].strip()

            # Clean and validate
            explanation = clean_alternative_response(explanation, field_name)

            if is_valid_explanation(explanation, field_name):
                print(f"✅ Réponse générée: {explanation}", file=sys.stderr)
                return explanation

        print("⚠️ Réponse du modèle alternatif invalide", file=sys.stderr)
        return None

    except Exception as e:
        print(f"❌ Erreur modèle alternatif: {str(e)}", file=sys.stderr)
        return None

def clean_alternative_response(response, field_name):
    """Clean response from alternative models"""
    if not response:
        return ""

    cleaned = response.strip()

    # Remove common unwanted patterns
    unwanted_patterns = [
        "Réponse courte:", "Expliquez", "Dans le champ", "Pour le champ",
        "Ce champ", "Le champ", "Il faut", "Vous devez"
    ]

    for pattern in unwanted_patterns:
        if cleaned.startswith(pattern):
            cleaned = cleaned[len(pattern):].strip()
            if cleaned.startswith(":"):
                cleaned = cleaned[1:].strip()

    # Ensure proper format
    if cleaned and not cleaned[0].isupper():
        cleaned = cleaned[0].upper() + cleaned[1:]

    # Ensure it starts with action verb
    action_verbs = ["saisissez", "indiquez", "précisez", "complétez", "entrez", "remplissez"]
    if not any(cleaned.lower().startswith(verb) for verb in action_verbs):
        cleaned = "Saisissez " + cleaned.lower()

    # Ensure proper ending
    if not cleaned.endswith("."):
        cleaned += "."

    return cleaned

def explain_field(field_name):
    """Main function to explain field with multiple fallback strategies"""

    # Strategy 1: Check authentication and try Mistral if available
    is_authenticated, auth_info = check_hf_authentication()

    if is_authenticated:
        print(f"✅ Authentification HF détectée: {auth_info.get('name', 'Utilisateur')}", file=sys.stderr)
        try:
            return try_mistral_model(field_name)
        except Exception as e:
            print(f"❌ Mistral a échoué malgré l'authentification: {str(e)}", file=sys.stderr)
    else:
        print("⚠️ Pas d'authentification Hugging Face détectée", file=sys.stderr)

    # Strategy 2: Try alternative free models
    alternative_result = try_alternative_model(field_name)
    if alternative_result:
        return alternative_result

    # Strategy 3: Use perfect fallback
    print("🔄 Utilisation du système de fallback optimisé", file=sys.stderr)
    return get_perfect_fallback(field_name)

def try_mistral_model(field_name):
    """Try the original Mistral model with authentication"""
    model_name = "mistralai/Mistral-7B-Instruct-v0.2"

    # Check for local model first
    local_model_path = "./models/mistral-7b-instruct"
    if os.path.exists(local_model_path):
        model_name = local_model_path
        print(f"✅ Utilisation du modèle local: {local_model_path}", file=sys.stderr)

    prompt = create_mistral_prompt(field_name)

    print("🔄 Chargement du tokenizer et modèle Mistral...", file=sys.stderr)

    # Load with authentication token from environment or cache
    tokenizer = AutoTokenizer.from_pretrained(
        model_name,
        trust_remote_code=True,
        padding_side="left",
        cache_dir="./models/mistral-7b-instruct"
    )

    if tokenizer.pad_token is None:
        tokenizer.pad_token = tokenizer.eos_token

    model = AutoModelForCausalLM.from_pretrained(
        model_name,
        trust_remote_code=True,
        torch_dtype=torch.float16 if torch.cuda.is_available() else torch.float32,
        device_map="auto" if torch.cuda.is_available() else "cpu",
        low_cpu_mem_usage=True,
        cache_dir="./models/mistral-7b-instruct",
        max_parallel_downloads=1  # Force sequential downloads
    )

    print("🚀 Génération avec Mistral...", file=sys.stderr)

    inputs = tokenizer(prompt, return_tensors="pt", padding=True)

    with torch.no_grad():
        outputs = model.generate(
            **inputs,
            max_new_tokens=30,
            temperature=0.1,
            top_p=0.8,
            do_sample=True,
            repetition_penalty=1.2,
            pad_token_id=tokenizer.eos_token_id,
            eos_token_id=tokenizer.eos_token_id,
            early_stopping=True
        )

    full_response = tokenizer.decode(outputs[0], skip_special_tokens=True)

    if "[/INST]" in full_response:
        response = full_response.split("[/INST]")[-1].strip()
    else:
        response = full_response.strip()

    explanation = clean_mistral_response(response, field_name)

    if is_valid_explanation(explanation, field_name):
        return explanation
    else:
        raise Exception("Generated response was invalid")

def create_mistral_prompt(field_name):
    """Create perfectly optimized prompt for Mistral 7B"""
    context = get_field_context(field_name)

    prompt = f"""<s>[INST] Tu es un expert RH français. Un utilisateur ne comprend pas quoi saisir dans le champ "{field_name}" d'un formulaire d'embauche.

CONTEXTE:
- Définition: {context['definition']}
- Format attendu: {context['format']}
- Exemple: {context['exemple']}

CONSIGNE: Écris UNE SEULE phrase claire et directe qui explique exactement ce que l'utilisateur doit saisir. La phrase doit:
1. Commencer par un verbe d'action (Saisissez, Indiquez, Précisez)
2. Être en français correct
3. Être pratique et actionnable
4. Faire moins de 20 mots

Réponds UNIQUEMENT par cette phrase, sans introduction ni explication supplémentaire. [/INST]</s>"""

    return prompt

def clean_mistral_response(response, field_name):
    """Clean Mistral response to get perfect output"""
    if not response or len(response.strip()) == 0:
        return ""

    cleaned = response.strip()

    # Remove any unwanted prefixes/suffixes
    unwanted_patterns = [
        "Dans le champ", "Pour le champ", "Ce champ", "Le champ",
        "Vous devez", "Il faut", "Il convient de", "Veuillez",
        "[INST]", "[/INST]", "<s>", "</s>"
    ]

    for pattern in unwanted_patterns:
        if cleaned.startswith(pattern):
            cleaned = cleaned[len(pattern):].strip()
            if cleaned.startswith(("le ", "la ", "les ", "un ", "une ", ": ")):
                parts = cleaned.split(" ", 1)
                if len(parts) > 1:
                    cleaned = parts[1]

    # Ensure proper capitalization
    if cleaned and not cleaned[0].isupper():
        cleaned = cleaned[0].upper() + cleaned[1:]

    # Ensure it starts with action verb
    action_verbs = ["saisissez", "indiquez", "précisez", "complétez", "entrez", "remplissez"]
    if not any(cleaned.lower().startswith(verb) for verb in action_verbs):
        cleaned = "Saisissez " + cleaned.lower()

    # Take only first sentence
    if "." in cleaned:
        cleaned = cleaned.split(".")[0] + "."
    elif not cleaned.endswith("."):
        cleaned += "."

    return cleaned

def is_valid_explanation(explanation, field_name):
    """Validate if explanation is good"""
    if not explanation or len(explanation) < 10:
        return False

    # Must start with action verb
    action_verbs = ["saisissez", "indiquez", "précisez", "complétez", "entrez", "remplissez"]
    if not any(explanation.lower().startswith(verb) for verb in action_verbs):
        return False

    # Shouldn't contain unwanted elements
    unwanted = ["[INST]", "[/INST]", "<s>", "</s>", "contexte:", "exemple:", "définition:"]
    if any(unwanted_item in explanation.lower() for unwanted_item in unwanted):
        return False

    # Should be reasonable length (not too long)
    if len(explanation.split()) > 25:
        return False

    return True

def get_perfect_fallback(field_name):
    """Perfect fallback responses"""
    fallbacks = {
        "codesociete": "Saisissez le numéro SIREN (9 chiffres) ou SIRET (14 chiffres) de votre entreprise.",
        "matriculesalarie": "Indiquez le matricule unique attribué à ce salarié par votre service RH (ex: QAAZE001).",
        "nomusuel": "Saisissez le nom utilisé au quotidien par la personne (peut différer du nom de naissance).",
        "nompatronymique": "Indiquez le nom de famille officiel tel qu'il apparaît sur les documents d'identité.",
        "prenom": "Saisissez le prénom principal de la personne tel qu'elle souhaite être appelée.",
        "deuxiemeprenom": "Indiquez le deuxième prénom si la personne en utilise un (optionnel).",
        "numeroinsee": "Saisissez le numéro de sécurité sociale (13 chiffres + 2 chiffres de clé).",
        "villenaissance": "Indiquez la ville ou commune de naissance selon l'état civil.",
        "complement1": "Précisez le nom de résidence, bâtiment ou étage si applicable.",
        "complement2": "Ajoutez le numéro d'appartement ou autres précisions d'adresse.",
        "lieudit": "Indiquez le nom du hameau ou lieu-dit pour les adresses rurales.",
        "codepostal": "Saisissez le code postal à 5 chiffres de la commune de résidence.",
        "commune": "Indiquez le nom exact de la ville ou commune selon l'INSEE.",
        "codeinseecommune": "Saisissez le code INSEE à 5 chiffres de la commune.",
        "numerovoie": "Indiquez le numéro dans la rue, avenue ou boulevard.",
        "poste": "Précisez l'intitulé exact du poste à occuper dans l'entreprise.",
        "emploi": "Indiquez la catégorie d'emploi (Cadre, Technicien, Agent de maîtrise...).",
        "uniteorganisationnelle": "Précisez le service, département ou équipe de rattachement.",
        "codecycle": "Saisissez le code de référence du cycle de travail appliqué.",
        "accordentreprise": "Indiquez la référence de l'accord d'entreprise applicable si existant.",
        "duree": "Précisez la durée du contrat (12 mois, 2 ans, indéterminée...).",
        "modalitehoraire": "Décrivez l'organisation des horaires (fixes, variables, forfait jour...).",
        "forfaitjours": "Indiquez le nombre de jours travaillés par an (entre 200 et 218).",
        "forfaitheures": "Saisissez le volume annuel d'heures contractuel (ex: 1607h).",
        "heurestravaillees": "Indiquez le nombre d'heures hebdomadaires effectivement travaillées.",
        "heurespayees": "Précisez le nombre d'heures rémunérées (peut différer des heures travaillées)."
    }

    # Normalisation pour recherche
    normalized_key = field_name.lower().replace(" ", "").replace("_", "").replace("-", "")

    if normalized_key in fallbacks:
        return fallbacks[normalized_key]

    # Fallback générique basé sur le contexte
    context = get_field_context(field_name)
    return f"Saisissez {context['definition'].lower()} selon le format: {context['format'].lower()}."

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(json.dumps({"error": "Usage: python explain_field.py '<field_name>'"}), file=sys.stderr)
        print("", file=sys.stderr)
        print("Pour résoudre l'erreur d'authentification Hugging Face:", file=sys.stderr)
        print("1. Créez un compte sur https://huggingface.co", file=sys.stderr)
        print("2. Demandez l'accès au modèle Mistral-7B-Instruct-v0.2", file=sys.stderr)
        print("3. Installez huggingface_hub: pip install huggingface_hub", file=sys.stderr)
        print("4. Connectez-vous: huggingface-cli login", file=sys.stderr)
        print("", file=sys.stderr)
        print("Ou utilisez le script tel quel - il utilisera des modèles alternatifs.", file=sys.stderr)
        sys.exit(1)

    field_name = sys.argv[1]
    explanation = explain_field(field_name)
    print(json.dumps({"explanation": explanation}))