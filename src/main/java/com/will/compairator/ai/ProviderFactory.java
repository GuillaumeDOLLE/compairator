package com.will.compairator.ai;

import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.exception.UnknownProviderException;
import com.will.compairator.configuration.AiProperties;
import com.will.compairator.configuration.AiProviderConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;

@Component
public class ProviderFactory {

    // map l'enum provider à l'instance d'un provider
    private EnumMap<AiProvider, ProviderAi> providersMap;

    // Tous les beans qui héritent de ProviderAi seront dans cette liste
    public ProviderFactory(List<ProviderAi> providers) {

        // les clef de cette map seront des valeurs de l'enum AiProvicer
        this.providersMap = new EnumMap<>(AiProvider.class);

        // pour chaque provider j'ajoute l'instance du provider à la key
        // GROQ = GroqAi
        // Mistral = MistralAi
        for (ProviderAi provider : providers) {
            this.providersMap.put(provider.getProvider(), provider);
        }
    }

    // Permet de récupérer le provider selon l'enum | GROQ -> GroqAi | Mistral -> MistralAi
    // Evite le switch à rallonge
    public ProviderAi getProvider(AiProvider aiProvider) /* throws UnknownProviderException */ {
//        try {
            return this.providersMap.get(aiProvider);
//        } catch (Exception e) {
//            throw new UnknownProviderException("Ce provider est inconnu");
//        }
    }

}
