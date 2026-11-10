# Game Explorer 🎮

Aplicativo Android de catálogo de jogos gratuitos, com pesquisa, detalhes, favoritos e perfil. Desenvolvido em **Kotlin e Jetpack Compose** para o projeto final da 3ª etapa do COTEMIG.

## API utilizada

**FreeToGame** — https://www.freetogame.com/api-doc

- Base: `https://www.freetogame.com/api/`
- `GET /games`: catálogo de jogos, com capa, gênero e plataforma.
- `GET /game?id=452`: detalhes do jogo selecionado.
- API pública, sem cadastro nem chave; oferece jogos free-to-play, não necessariamente títulos pagos do Figma.
- Crédito da API: [FreeToGame.com](https://www.freetogame.com/).

## Funcionalidades

- Splash/boas-vindas; cadastro e login locais com e-mail válido e senha de no mínimo seis caracteres.
- Senha **não é salva em texto puro**: hash SHA-256 com salt aleatório por usuário (solução didática, não substitui autenticação de servidor).
- Persistência da sessão com DataStore: o usuário não precisa entrar novamente ao reabrir o aplicativo.
- Catálogo via Retrofit, executado fora da Main Thread por coroutines, com estados carregando/sucesso/erro e botão de tentar novamente.
- Busca por título ou gênero, destaques e listagem.
- Detalhe individual com rota type-safe (`Details(id: Int)`).
- Favoritos por usuário persistidos em banco Room.
- Barra inferior com **três abas**: Início, Favoritos e Perfil.
- Perfil com nome, e-mail, quantidade de favoritos, edição do nome e saída.

## Como abrir e executar

1. Abra a pasta `GameExplorer` no **Android Studio**.
2. Configure o JDK 17 em `Settings > Build Tools > Gradle`, se necessário.
3. Aguarde a sincronização do Gradle (requer Internet) e instale o SDK 35, se solicitado.
4. Execute no emulador Android (API 26+) ou celular.
5. Cadastre um usuário e use a busca, detalhes e favoritos.

> Este ZIP contém o código-fonte e arquivos Gradle; o **Gradle Wrapper JAR não está incluído**. O Android Studio pode usar seu Gradle local ou você pode gerar o wrapper com `gradle wrapper --gradle-version 8.9`. A compilação não foi executada neste ambiente; valide a sincronização e ajustes do SDK no Android Studio.

## Organização por camadas

```text
app/src/main/java/com/gameexplorer/app/
├── network/         # Interface Retrofit e DTOs
├── data/            # Room: Entity, DAO, Database e Repository; sessão local
├── viewmodel/       # AppViewModel e estados de tela
├── navigation/      # Rotas Navigation Compose type-safe
├── userinterface/   # Telas Compose, componentes e paleta
└── MainActivity.kt
```

**Regra de dependência:** a interface chama o ViewModel; o ViewModel chama os repositories; somente `data` chama o Retrofit e o Room. Nenhuma tela conhece `GameApi` ou `DAO`.

## Prints para a entrega

Adicione **4–6 capturas reais** em `prints/`: login, cadastro, catálogo, detalhes, favoritos e erro de rede. Prints não foram inventados ou incluídos porque exigem execução no emulador.

## Divisão de tarefas

| Integrante | O que fez |
|---|---|
| Integrante 1 | Preencher com contribuição real |
| Integrante 2 | Preencher com contribuição real |
| Integrante 3 (se houver) | Preencher com contribuição real |

## O que aprendemos

- Integrante 1: preencher após realizar o trabalho.
- Integrante 2: preencher após realizar o trabalho.
- Integrante 3 (se houver): preencher após realizar o trabalho.

## Segurança e limitações

A autenticação é local, conforme pedido pelo professor. O hash SHA-256 com salt atende ao exemplo didático da atividade, mas **não é adequado para produção** (prefira servidor e algoritmo de derivação de senha resistente, como Argon2id). A API requer acesso à Internet; não há cache offline do catálogo. Jogos do protótipo como God of War podem não aparecer porque FreeToGame cataloga jogos gratuitos.
