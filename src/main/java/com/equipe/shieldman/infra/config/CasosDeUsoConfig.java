package com.equipe.shieldman.infra.config;

import com.equipe.shieldman.application.ConsultarEscudo;
import com.equipe.shieldman.application.GerenciarEscudos;
import com.equipe.shieldman.application.GerenciarMestres;
import com.equipe.shieldman.domain.repository.EscudoRepository;
import com.equipe.shieldman.domain.repository.MestreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/** Monta o caso de uso com o repositório PostgreSQL selecionado pelo CDI. */
@ApplicationScoped
public class CasosDeUsoConfig {
    @Produces
    public GerenciarMestres gerenciarMestres(MestreRepository mestres) {
        return new GerenciarMestres(mestres);
    }

    @Produces
    public GerenciarEscudos gerenciarEscudos(EscudoRepository escudos, MestreRepository mestres) {
        return new GerenciarEscudos(escudos, mestres);
    }

    @Produces
    public ConsultarEscudo consultarEscudo(EscudoRepository repository) {
        return new ConsultarEscudo(repository);
    }
}
