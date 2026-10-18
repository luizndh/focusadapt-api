package com.focus.adapt.api.infrastructure.integration.gemini;

import org.springframework.stereotype.Component;

import com.focus.adapt.api.domain.entity.enums.ContentBlockType;
import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile;

@Component
public class PromptBuilder {

    public String buildStudyPlanPrompt(String category, String description, int availableMinutesPerDay,
                                       String availableDays, String startDate,
                                       ProcrastinationProfile profileType) {
        String profileInstructions = getPlanProfileInstructions(profileType);
        String dailyTimeFormatted = formatDailyStudyTime(availableMinutesPerDay);

        return String.format("""
                Você é um especialista em elaboração de currículos acadêmicos e planejamento de estudos.

                Seu objetivo é criar um currículo de estudos personalizado e de alta qualidade para o estudante.

                ==================================================
                ARQUITETURA FUNDAMENTAL E DEFINIÇÃO DE TAREFAS
                ==================================================

                * O PLANO DE ESTUDOS (STUDY PLAN) representa o assunto geral que o estudante deseja aprender.
                * Cada TAREFA DE ESTUDO (STUDY TASK) representa um único TÓPICO ou CONCEITO ACADÊMICO coerente dentro desse assunto.
                * Uma STUDY TASK NÃO é um exercício, desafio, quiz, missão, recompensa ou atividade de gamificação.
                * O título de cada StudyTask deve descrever APENAS o conceito acadêmico em si (ex: "Filtragem com filter()", NÃO "Quiz de filter()", NÃO "Desafio 1").
                * NÃO coloque "Dia 1", "Dia 2", datas, dias da semana ou frases motivacionais nos títulos ou descrições das StudyTasks.
                * As atividades pedagógicas (quizzes, exercícios, checkpoints, desafios) serão geradas posteriormente dentro dos ContentBlocks, NÃO na definição do plano.

                Informações do Estudante:
                * Tópico principal: "%s"
                * Contexto adicional: "%s"
                * Tempo de estudo disponível por dia: %s
                * Dias da semana disponíveis: %s
                * Data de início: %s
                * Perfil de Procrastinação: %s

                ==================================================
                ESTRUTURAÇÃO DO CURRÍCULO ESPECÍFICA POR PERFIL
                ==================================================

                Ambos os perfis estudam os mesmos conceitos acadêmicos fundamentais. A estrutura do currículo deve se adaptar às características comportamentais do estudante:

                %s

                ==================================================
                DURAÇÃO E GRANULARIDADE DAS TAREFAS DE ESTUDO
                ==================================================

                * Cada StudyTask deve representar aproximadamente de 15 a 45 minutos de estudo focado.
                * NUNCA crie tarefas massivas de centenas de minutos.
                * Se um conceito for muito amplo, divida-o em subconceitos logicamente coesos.
                * Não fragmente artificialmente um único conceito simples em múltiplas tarefas triviais.

                ==================================================
                REGRAS DE CRONOGRAMA E CALENDÁRIO
                ==================================================

                * Distribua as tarefas de estudo estritamente de acordo com os parâmetros declarados pelo estudante.
                * A primeira tarefa deve ser agendada na data de início declarada ou após ela, correspondendo a um dos dias da semana disponíveis do estudante.
                * Atribua uma `scheduledDate` válida e sequencial (no formato YYYY-MM-DD) para cada StudyTask, agendando tarefas APENAS nos dias da semana disponíveis do estudante.
                * NÃO exceda o tempo diário de estudo declarado em nenhuma data do calendário.
                * Defina a `startDate` (data da primeira tarefa) e a `endDate` (data da última tarefa) gerais do plano com precisão com base no cronograma.

                ==================================================
                SELEÇÃO DA CATEGORIA DO ASSUNTO
                ==================================================

                O campo "category" deve ser escolhido a partir da seguinte lista controlada:
                * Matemática
                * Física
                * Química
                * Biologia
                * História
                * Geografia
                * Sociologia
                * Filosofia
                * Língua Portuguesa e Literatura
                * Redação
                * Idiomas
                * Tecnologia da Informação
                * Programação e Desenvolvimento de Software
                * Ciência de Dados e Inteligência Artificial
                * Engenharia e Arquitetura
                * Medicina e Ciências da Saúde
                * Psicologia e Comportamento
                * Direito e Legislação
                * Economia, Finanças e Contabilidade
                * Administração, Negócios e Empreendedorismo
                * Marketing e Comunicação
                * Artes, Música e Design
                * Educação e Pedagogia
                * Astronomia e Geociências
                * Meio Ambiente e Ecologia
                * Produtividade e Desenvolvimento Pessoal
                * Concursos e Vestibulares
                * Outros

                ==================================================
                FORMATO DE SAÍDA
                ==================================================

                Retorne APENAS um JSON válido, sem blocos de código Markdown (sem ```json).
                Não inclua explicações ou textos fora do objeto JSON.

                Schema:
                {
                    "title": "Nome do Tópico",
                    "description": "Visão geral do currículo do plano de estudos.",
                    "category": "Uma Categoria da Lista",
                    "startDate": "YYYY-MM-DD",
                    "endDate": "YYYY-MM-DD",
                    "totalEstimatedDurationMinutes": 180,
                    "tasks": [
                        {
                            "title": "Título do Conceito Acadêmico",
                            "description": "Breve explicação do tópico acadêmico abordado.",
                            "order": 1,
                            "estimatedDurationMinutes": 30,
                            "scheduledDate": "YYYY-MM-DD"
                        }
                    ]
                }
                """,
            category,
            description != null ? description : "N/A",
            dailyTimeFormatted,
            availableDays != null ? availableDays : "Qualquer dia",
            startDate != null ? startDate : "Amanhã",
            profileType.name(),
            profileInstructions
        );
    }

    /**
     * Constrói o prompt para geração de plano de estudos a partir de um documento PDF.
     * Reutiliza as mesmas regras acadêmicas, de duração (15-45m), perfil comportamental e calendário.
     */
    public String buildStudyPlanFromPdfPrompt(String topicHint, String description, int availableMinutesPerDay,
                                              String availableDays, String startDate,
                                              ProcrastinationProfile profileType) {
        String profileInstructions = getPlanProfileInstructions(profileType);
        String dailyTimeFormatted = formatDailyStudyTime(availableMinutesPerDay);

        return String.format("""
                Você é um especialista em elaboração de currículos acadêmicos e planejamento de estudos analisando o documento PDF anexado.

                Seu objetivo é extrair o tema central, capítulos, conceitos e objetivos de aprendizagem do documento PDF anexado e organizá-los em um currículo de estudos personalizado e de alta qualidade para o estudante.

                ==================================================
                INSTRUÇÕES DE ANÁLISE DO CONTEÚDO DO PDF
                ==================================================

                * Analise o texto, a estrutura visual, os capítulos, as seções, os diagramas e as tabelas do PDF anexado.
                * Estruture o plano de estudos seguindo o currículo lógico e progressivo apresentado no PDF.
                * Se uma dica de tópico for fornecida, use-a para priorizar as seções relevantes; caso contrário, determine o título a partir do documento.

                ==================================================
                ARQUITETURA FUNDAMENTAL E DEFINIÇÃO DE TAREFAS
                ==================================================

                * O PLANO DE ESTUDOS (STUDY PLAN) representa o assunto geral abordado no PDF.
                * Cada TAREFA DE ESTUDO (STUDY TASK) representa um único TÓPICO ou CONCEITO ACADÊMICO coerente extraído do PDF.
                * Uma STUDY TASK NÃO é um exercício, desafio, quiz, missão, recompensa ou atividade de gamificação.
                * O título de cada StudyTask deve descrever APENAS o conceito acadêmico em si (ex: "Estruturas de Repetição", NÃO "Capítulo 1", NÃO "Quiz").
                * NÃO coloque "Dia 1", "Dia 2", datas, dias da semana ou frases motivacionais nos títulos ou descrições das StudyTasks.
                * As atividades pedagógicas (quizzes, exercícios, checkpoints, desafios) serão geradas posteriormente dentro dos ContentBlocks, NÃO na definição do plano.

                Informações do Estudante:
                * Dica de Tópico: "%s"
                * Contexto adicional: "%s"
                * Tempo de estudo disponível por dia: %s
                * Dias da semana disponíveis: %s
                * Data de início: %s
                * Perfil de Procrastinação: %s

                ==================================================
                ESTRUTURAÇÃO DO CURRÍCULO ESPECÍFICA POR PERFIL
                ==================================================

                Ambos os perfis estudam os mesmos conceitos acadêmicos fundamentais. A estrutura do currículo deve se adaptar às características comportamentais do estudante:

                %s

                ==================================================
                DURAÇÃO E GRANULARIDADE DAS TAREFAS DE ESTUDO
                ==================================================

                * Cada StudyTask deve representar aproximadamente de 15 a 45 minutos de estudo focado.
                * NUNCA crie tarefas massivas de centenas de minutos.
                * Se um capítulo ou conceito no PDF for muito amplo, divida-o em subconceitos logicamente coesos.
                * Não fragmente artificialmente um único conceito simples em múltiplas tarefas triviais.

                ==================================================
                REGRAS DE CRONOGRAMA E CALENDÁRIO
                ==================================================

                * Distribua as tarefas de estudo estritamente de acordo com os parâmetros declarados pelo estudante.
                * A primeira tarefa deve ser agendada na data de início declarada ou após ela, correspondendo a um dos dias da semana disponíveis do estudante.
                * Atribua uma `scheduledDate` válida e sequencial (no formato YYYY-MM-DD) para cada StudyTask, agendando tarefas APENAS nos dias da semana disponíveis do estudante.
                * NÃO exceda o tempo diário de estudo declarado em nenhuma data do calendário.
                * Defina a `startDate` (data da primeira tarefa) e a `endDate` (data da última tarefa) gerais do plano com precisão com base no cronograma.

                ==================================================
                SELEÇÃO DA CATEGORIA DO ASSUNTO
                ==================================================

                O campo "category" deve ser escolhido a partir da seguinte lista controlada:
                * Matemática
                * Física
                * Química
                * Biologia
                * História
                * Geografia
                * Sociologia
                * Filosofia
                * Língua Portuguesa e Literatura
                * Redação
                * Idiomas
                * Tecnologia da Informação
                * Programação e Desenvolvimento de Software
                * Ciência de Dados e Inteligência Artificial
                * Engenharia e Arquitetura
                * Medicina e Ciências da Saúde
                * Psicologia e Comportamento
                * Direito e Legislação
                * Economia, Finanças e Contabilidade
                * Administração, Negócios e Empreendedorismo
                * Marketing e Comunicação
                * Artes, Música e Design
                * Educação e Pedagogia
                * Astronomia e Geociências
                * Meio Ambiente e Ecologia
                * Produtividade e Desenvolvimento Pessoal
                * Concursos e Vestibulares
                * Outros

                ==================================================
                FORMATO DE SAÍDA
                ==================================================

                Retorne APENAS um JSON válido, sem blocos de código Markdown (sem ```json).
                Não inclua explicações ou textos fora do objeto JSON.

                Schema:
                {
                    "title": "Nome do Tópico Extraído do PDF",
                    "description": "Visão geral do currículo do plano de estudos com base no conteúdo do PDF.",
                    "category": "Uma Categoria da Lista",
                    "startDate": "YYYY-MM-DD",
                    "endDate": "YYYY-MM-DD",
                    "totalEstimatedDurationMinutes": 180,
                    "tasks": [
                        {
                            "title": "Título do Conceito Acadêmico",
                            "description": "Breve explicação do tópico acadêmico abordado no PDF.",
                            "order": 1,
                            "estimatedDurationMinutes": 30,
                            "scheduledDate": "YYYY-MM-DD"
                        }
                    ]
                }
                """,
            topicHint != null ? topicHint : "Extraído do PDF",
            description != null ? description : "Baseado no documento PDF anexado",
            dailyTimeFormatted,
            availableDays != null ? availableDays : "Qualquer dia",
            startDate != null ? startDate : "Amanhã",
            profileType.name(),
            profileInstructions
        );
    }

    private String formatDailyStudyTime(int minutes) {
        if (minutes < 60) {
            return minutes + " minutos";
        }
        int hours = minutes / 60;
        int rem = minutes % 60;
        if (rem == 0) {
            return hours + (hours == 1 ? " hora" : " horas") + " (" + minutes + " minutos)";
        }
        return hours + (hours == 1 ? " hora e " : " horas e ") + rem + " minutos (" + minutes + " minutos no total)";
    }

    /**
     * ETAPA 1: Gera a estrutura pedagógica de ContentBlocks para uma StudyTask (sem gerar o texto completo).
     */
    public String buildTaskBlocksStructurePrompt(String taskTitle, String taskDescription,
                                                 String planTitle, ProcrastinationProfile profileType) {
        String structureGuidelines = getBlocksStructureProfileGuidelines(profileType);

        return String.format("""
                Você é um designer instrucional especialista estruturando os blocos de aprendizagem (ContentBlocks) para uma tarefa de estudo acadêmica específica.

                Contexto:
                * Plano de Estudos: "%s"
                * Tarefa de Estudo Acadêmica (StudyTask): "%s"
                * Descrição da Tarefa: "%s"
                * Perfil de Procrastinação do Estudante: %s

                ==================================================
                ARQUITETURA DOS BLOCOS DE CONTEÚDO
                ==================================================

                Uma StudyTask representa o que o estudante aprende. Os ContentBlocks representam como a aula é entregue em pequenas partes.

                Tipos de Bloco Possíveis:
                * EXPLANATION: Explicação conceitual, teorias ou princípios.
                * EXAMPLE: Exemplos práticos, estudos de caso reais, cenários comportamentais ou ilustrações concretas adaptadas ao domínio.
                * GUIDED_EXERCISE: Prática guiada passo a passo com soluções.
                * QUIZ: Perguntas rápidas com feedback imediato.
                * FLASHCARD: Pares de pergunta/resposta para recordação ativa.
                * CHALLENGE: Microdesafio ou problema para resolver.
                * CHECKPOINT: Autoavaliação de marco para confirmar a compreensão.
                * SUMMARY: Principais conclusões e encerramento.

                ==================================================
                ESTRATÉGIA DE ESTRUTURAÇÃO ESPECÍFICA POR PERFIL
                ==================================================

                %s

                ==================================================
                REGRAS
                ==================================================

                1. Cada bloco deve ter uma duração estimada de 5 a 10 minutos.
                2. Os títulos dos blocos devem ser claros, engajadores e em Português (pt-BR).
                3. ADAPTAÇÃO AO DOMÍNIO: O plano de estudos pode ser sobre QUALQUER assunto (psicologia, negócios, saúde, direito, idiomas, história, ciências, programação, etc.). Adapte todos os títulos e tipos de blocos ao domínio real.
                4. NÃO gere o texto completo do conteúdo da aula nesta etapa. Gere apenas a estrutura dos blocos.

                ==================================================
                FORMATO DE SAÍDA
                ==================================================

                Retorne APENAS um JSON válido (sem blocos de código markdown).

                Schema:
                {
                    "blocks": [
                        {
                            "order": 1,
                            "type": "EXPLANATION",
                            "title": "Título do Bloco em Português",
                            "estimatedMinutes": 5
                        },
                        {
                            "order": 2,
                            "type": "EXAMPLE",
                            "title": "Título do Bloco em Português",
                            "estimatedMinutes": 5
                        }
                    ]
                }
                """,
            planTitle,
            taskTitle,
            taskDescription != null ? taskDescription : "N/A",
            profileType.name(),
            structureGuidelines
        );
    }

    /**
     * ETAPA 2: Gera o conteúdo em Markdown de um único ContentBlock sob demanda.
     */
    public String buildBlockContentPrompt(String planTitle, String taskTitle, String taskDescription,
                                          String blockTitle, ContentBlockType blockType,
                                          ProcrastinationProfile profileType) {
        String typeGuidelines = getBlockTypeSpecificGuidelines(blockType);
        String profilePedagogy = getBlockContentProfilePedagogy(profileType);

        return String.format("""
                Você é um educador especialista gerando o conteúdo para um único bloco de aprendizagem focado (ContentBlock) em um aplicativo de ensino.

                Contexto da Aula:
                * Plano de Estudos: "%s"
                * Tópico da Tarefa (StudyTask): "%s"
                * Descrição da Tarefa: "%s"
                * Título do ContentBlock: "%s"
                * Tipo do ContentBlock: %s
                * Perfil de Procrastinação do Estudante: %s

                ==================================================
                DIRETRIZES DO TIPO DE BLOCO: %s
                ==================================================

                %s

                ==================================================
                ESTRATÉGIA PEDAGÓGICA DO PERFIL: %s
                ==================================================

                %s

                ==================================================
                REGRAS E ESCOPO ESTRITO
                ==================================================

                1. Gere APENAS Markdown puro e bem formatado em Português (Brasil - pt-BR).
                2. NÃO envolva em JSON. NÃO inclua blocos de código markdown ao redor de toda a saída.
                3. Comece diretamente com `# %s`.
                4. Seja direto, claro e agregue alto valor. Este bloco deve levar aproximadamente de 5 a 10 minutos para ser lido/concluído.
                5. ADAPTAÇÃO AO DOMÍNIO: O assunto pode ser sobre QUALQUER área (ex: Psicologia, Produtividade, Direito, Saúde, Idiomas, Ciências, Negócios, Programação, etc.). Use código de programação APENAS se o Plano de Estudos for explicitamente sobre desenvolvimento de software. Para todos os outros assuntos, use estudos de caso realistas, cenários práticos, técnicas comportamentais, diálogos ou exemplos específicos do domínio.
                6. ESCOPO ESTRITO DO BLOCO - SEM SEÇÕES EXTRAS: Mantenha-se estritamente dentro do tipo de ContentBlock solicitado (%s). NÃO adicione seções extras ao final (como 'Desafio Rápido', 'Quiz', 'Exercícios para você', 'Próximos Passos' ou 'Desafio Prático'). O estudante possui outros blocos dedicados para quizzes e desafios.
                7. NUNCA use frases conversacionais de chatbot (ex: "Espero que tenha gostado", "Me diga se tem dúvidas", "Quer continuar?", "Até a próxima aula!").
                """,
            planTitle,
            taskTitle,
            taskDescription != null ? taskDescription : "N/A",
            blockTitle,
            blockType.name(),
            profileType.name(),
            blockType.name(),
            typeGuidelines,
            profileType.name(),
            profilePedagogy,
            blockTitle,
            blockType.name()
        );
    }

    private String getPlanProfileInstructions(ProcrastinationProfile profileType) {
        return switch (profileType) {
            case PERFECTIONIST -> """
                    DIRETRIZES DE CURRÍCULO PARA O PERFIL PERFECTIONIST:
                    * O estudante apresenta medo de falhar, autocrítica, dificuldade para começar e possível sobrecarga com tópicos muito amplos.
                    * Estruture o currículo com uma progressão gradual e clara, dos fundamentos aos tópicos avançados.
                    * Defina escopos acadêmicos precisos e claramente delimitados para cada tarefa, para que o estudante tenha pontos de conclusão claros.
                    * Evite combinar múltiplos objetivos de aprendizagem distintos em uma única StudyTask.
                    * Mantenha a duração individual das tarefas entre 15 e 45 minutos para evitar fadiga cognitiva e paralisia por análise.""";
            case STIMULUS_SEEKER -> """
                    DIRETRIZES DE CURRÍCULO PARA O PERFIL STIMULUS_SEEKER:
                    * O estudante sente tédio rapidamente, busca novidades e responde fortemente a marcadores frequentes de progresso e aplicação prática.
                    * Mantenha as tarefas estritamente focadas em conceitos concretos, alternando entre teoria fundamental e aplicações práticas imediatas.
                    * Evite longos blocos consecutivos de teoria puramente abstrata onde a progressão prática for possível.
                    * Mantenha títulos acadêmicos estritos (ex: 'Filtragem com filter()', nunca 'Quiz de filter()' ou 'Jogo de Streams').
                    * Mantenha a duração individual das tarefas entre 15 e 45 minutos com marcos claros.""";
        };
    }

    private String getBlocksStructureProfileGuidelines(ProcrastinationProfile profileType) {
        return switch (profileType) {
            case PERFECTIONIST -> """
                    PREFERÊNCIAS DE ESTRUTURA PARA O PERFIL PERFECTIONIST:
                    * Enfatize a progressão linear, a previsibilidade e a segurança psicológica.
                    * Fluxo sugerido: EXPLANATION -> EXAMPLE -> GUIDED_EXERCISE -> CHECKPOINT -> EXAMPLE -> QUIZ -> SUMMARY.
                    * Garanta que cada conceito tenha um exemplo passo a passo e um exercício guiado antes de qualquer avaliação.
                    * Use blocos CHECKPOINT para validar a compreensão e reduzir a ambiguidade.""";
            case STIMULUS_SEEKER -> """
                    PREFERÊNCIAS DE ESTRUTURA PARA O PERFIL STIMULUS_SEEKER:
                    * Enfatize a brevidade, a novidade, a troca frequente de atividades e o feedback imediato.
                    * Fluxo sugerido: EXPLANATION -> EXAMPLE -> QUIZ -> CHALLENGE -> FLASHCARD -> EXAMPLE -> SUMMARY.
                    * Intercale blocos teóricos curtos com microdesafios interativos, quizzes e flashcards de recordação ativa.
                    * Mantenha todos os blocos dinâmicos e diretos (5-7 minutos cada).""";
        };
    }

    private String getBlockTypeSpecificGuidelines(ContentBlockType blockType) {
        return switch (blockType) {
            case EXPLANATION -> """
                    * Forneça uma explicação extremamente clara e estruturada do conceito central.
                    * Use analogias, conexões com o mundo real, tópicos (bullet points) e formatação limpa.
                    * Explique o 'porquê' e o 'como' sem enrolação desnecessária.
                    * Mantenha-se estritamente na explicação conceitual. NÃO adicione exercícios, quizzes ou desafios ao final.""";
            case EXAMPLE -> """
                    * Forneça um exemplo prático detalhado e realista, estudo de caso ou demonstração concreta adequada ao assunto.
                    * Se o tópico for desenvolvimento de software, use código limpo e bem comentado. Se o tópico não for técnico (ex: psicologia, negócios, produtividade, direito, saúde), use estudos de caso realistas, diálogos práticos, cenários comportamentais ou aplicações passo a passo do cotidiano ou da prática profissional.
                    * Detalhe o exemplo passo a passo com uma análise clara.
                    * NÃO adicione desafios, quizzes, tarefas rápidas ou seções de dever de casa ao final.""";
            case GUIDED_EXERCISE -> """
                    * Apresente um problema ou cenário prático e guie o estudante na sua resolução.
                    * Adapte ao domínio do assunto (ex: exercício comportamental, problema de negócios, cálculo ou tarefa de programação).
                    * Formate estritamente utilizando as seguintes seções:
                      ### Enunciado
                      [Descrição do problema, cenário ou situação prática]
                      ### Dica
                      [Dica para orientar o início do raciocínio]
                      ### Resolução
                      [Passo a passo completo e resolução explicada detalhadamente]""";
            case QUIZ -> """
                    * Forneça de 2 a 3 questões concisas de múltipla escolha diretamente relacionadas ao tópico da tarefa.
                    * Formate cada questão estritamente como:
                      ### Questão 1
                      [Enunciado da pergunta]
                      - (A) [Opção A]
                      - (B) [Opção B]
                      - (C) [Opção C]
                      - (D) [Opção D]
                      **Resposta Correta:** A
                      **Explicação:** [Explicação concisa do porquê está correta e as outras erradas]""";
            case FLASHCARD -> """
                    * Forneça de 2 a 4 flashcards de recordação ativa sobre os conceitos-chave.
                    * Formate cada cartão estritamente como:
                      ### Card 1
                      **Frente:** [Pergunta, termo ou conceito chave para recordar]
                      **Verso:** [Resposta direta, definição essencial ou aplicação chave]""";
            case CHALLENGE -> """
                    * Apresente um microdesafio conciso ou problema prático para resolver.
                    * Adapte ao assunto (ex: desafio de mudança comportamental, análise de cenário, problema de caso ou desafio técnico).
                    * Formate estritamente utilizando as seguintes seções:
                      ### Desafio
                      [Descrição do desafio e requisitos]
                      ### Dica
                      [Dica útil para começar]
                      ### Resolução
                      [Solução completa comentada e explicação]""";
            case CHECKPOINT -> """
                    * Um checkpoint de autoavaliação com 3 a 4 critérios de verificação para o estudante.
                    * Formate como:
                      ### Verificação de Compreensão
                      - [ ] [Critério 1 que o aluno deve ter compreendido ou conseguido aplicar]
                      - [ ] [Critério 2 que o aluno deve ter compreendido ou conseguido aplicar]
                      - [ ] [Critério 3 que o aluno deve ter compreendido ou conseguido aplicar]
                      **Mensagem:** [Mensagem encorajadora sobre o progresso no tema]""";
            case SUMMARY -> """
                    * De 3 a 5 tópicos (bullet points) resumindo as conclusões mais importantes e insights práticos.
                    * Uma 'Dica de Ouro' para aplicação do tópico no mundo real.""";
        };
    }

    private String getBlockContentProfilePedagogy(ProcrastinationProfile profileType) {
        return switch (profileType) {
            case PERFECTIONIST -> """
                    * Mantenha alta clareza e previsibilidade.
                    * Normalize erros e desmistifique casos extremos.
                    * Use orientação passo a passo e marcos estruturados e alcançáveis.""";
            case STIMULUS_SEEKER -> """
                    * Mantenha o texto dinâmico, conciso e focado no engajamento ativo.
                    * Use exemplos concretos antes da teoria abstrata.
                    * Entregue feedback imediato e conclusões práticas aplicáveis.""";
        };
    }
}
