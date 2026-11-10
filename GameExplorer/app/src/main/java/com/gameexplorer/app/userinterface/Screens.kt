package com.gameexplorer.app.userinterface

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil.compose.AsyncImage
import com.gameexplorer.app.data.FavoriteEntity
import com.gameexplorer.app.navigation.*
import com.gameexplorer.app.network.GameDetailDto
import com.gameexplorer.app.network.GameDto
import com.gameexplorer.app.viewmodel.AppViewModel
import com.gameexplorer.app.viewmodel.LoadState

@Composable fun GameExplorerApp(vm: AppViewModel) {
    val userId by vm.session.collectAsState()
    if (userId == -2L) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        // Recria o gráfico ao alternar entre área autenticada e área pública.
        key(userId >= 0) {
            val nav = rememberNavController()
            val start = if (userId >= 0) Home else Welcome
            Scaffold(containerColor = Background, bottomBar = {
                if (userId >= 0) GameBottomBar(nav)
            }) { padding ->
                NavHost(navController = nav, startDestination = start,
                    modifier = Modifier.padding(padding)) {
                    composable<Welcome> { WelcomeScreen { nav.navigate(Login) } }
                    composable<Login> { LoginScreen(vm, { nav.navigate(Register) }) }
                    composable<Register> { RegisterScreen(vm, { nav.popBackStack() }) }
                    composable<Home> { HomeScreen(vm) { nav.navigate(Details(it)) } }
                    composable<Favorites> { FavoritesScreen(vm) { nav.navigate(Details(it)) } }
                    composable<Profile> { ProfileScreen(vm) }
                    composable<Details> { entry ->
                        val id = entry.toRoute<Details>().id
                        LaunchedEffect(id) { vm.loadDetail(id) }
                        DetailsScreen(vm) { nav.popBackStack() }
                    }
                }
            }
        }
    }
}

@Composable private fun GameBottomBar(nav: NavHostController) {
    val entry by nav.currentBackStackEntryAsState()
    val destination = entry?.destination
    NavigationBar(containerColor = Surface) {
        val tabs = listOf(Triple(Home, "Início", Icons.Default.Home),
            Triple(Favorites, "Favoritos", Icons.Default.Favorite),
            Triple(Profile, "Perfil", Icons.Default.Person))
        tabs.forEachIndexed { i, tab ->
            val selected = when (i) {
                0 -> destination?.hasRoute<Home>() == true
                1 -> destination?.hasRoute<Favorites>() == true
                else -> destination?.hasRoute<Profile>() == true
            }
            NavigationBarItem(selected = selected, onClick = {
                nav.navigate(tab.first) { popUpTo(Home) { saveState = true }; launchSingleTop = true; restoreState = true }
            }, icon = { Icon(tab.third, contentDescription = tab.second) }, label = { Text(tab.second) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = Accent,
                    selectedTextColor = Accent, indicatorColor = Color(0xFF31244B)))
        }
    }
}

@Composable fun WelcomeScreen(onBegin: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Accent,
            modifier = Modifier.size(112.dp))
        Text("GAME", fontSize = 40.sp, fontWeight = FontWeight.Black)
        Text("EXPLORER", color = Primary, fontSize = 40.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(20.dp))
        Text("Descubra, explore e salve seus jogos favoritos.", color = Color.LightGray,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(40.dp))
        PrimaryButton("Começar", onBegin)
    }
}

@Composable private fun PrimaryButton(text: String, action: () -> Unit, enabled: Boolean = true) {
    Button(onClick = action, modifier = Modifier.fillMaxWidth().height(50.dp), enabled = enabled,
        shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable fun LoginScreen(vm: AppViewModel, onRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val error by vm.authError.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Logo()
        Spacer(Modifier.height(36.dp))
        Text("Entrar na sua conta", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Acesse seus jogos favoritos.", color = Color.Gray)
        Spacer(Modifier.height(26.dp))
        AppField(email, { email = it; vm.clearError() }, "E-mail", false, KeyboardType.Email)
        Spacer(Modifier.height(12.dp))
        AppField(password, { password = it; vm.clearError() }, "Senha", true)
        if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
        Spacer(Modifier.height(25.dp))
        PrimaryButton("Entrar", { vm.login(email, password) }, email.isNotBlank() && password.isNotBlank())
        TextButton(onClick = { vm.clearError(); onRegister() }) { Text("Não tem uma conta? Cadastre-se", color = Accent) }
    }
}

@Composable fun RegisterScreen(vm: AppViewModel, onLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }; var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }; var confirm by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val error by vm.authError.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Logo(); Spacer(Modifier.height(26.dp))
        Text("Criar conta", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Preencha os dados para começar.", color = Color.Gray)
        Spacer(Modifier.height(22.dp))
        AppField(name, { name = it; localError = null; vm.clearError() }, "Nome")
        Spacer(Modifier.height(10.dp))
        AppField(email, { email = it; localError = null; vm.clearError() }, "E-mail", keyboard = KeyboardType.Email)
        Spacer(Modifier.height(10.dp))
        AppField(password, { password = it; localError = null; vm.clearError() }, "Senha", true)
        Spacer(Modifier.height(10.dp))
        AppField(confirm, { confirm = it; localError = null; vm.clearError() }, "Confirmar senha", true)
        if (localError != null || error != null) Text(localError ?: error.orEmpty(),
            color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
        Spacer(Modifier.height(19.dp))
        PrimaryButton("Cadastrar", {
            if (password != confirm) localError = "As senhas não coincidem."
            else vm.register(name, email, password)
        })
        TextButton(onClick = { vm.clearError(); onLogin() }) { Text("Já tem uma conta? Entrar", color = Accent) }
    }
}

@Composable private fun Logo() {
    Icon(Icons.Default.SportsEsports, null, tint = Primary, modifier = Modifier.size(60.dp))
    Text("GAME EXPLORER", color = Accent, fontWeight = FontWeight.Black, fontSize = 25.sp)
}

@Composable private fun AppField(value: String, change: (String) -> Unit, label: String,
    password: Boolean = false, keyboard: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(value = value, onValueChange = change, singleLine = true,
        label = { Text(label) }, modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboard),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary,
            unfocusedContainerColor = Surface, focusedContainerColor = Surface),
        shape = RoundedCornerShape(12.dp))
}

@Composable fun HomeScreen(vm: AppViewModel, openDetails: (Int) -> Unit) {
    val state by vm.games.collectAsState()
    val user by vm.user.collectAsState()
    var query by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(18.dp))
        Text("Olá, ${user?.name?.substringBefore(' ') ?: "Jogador"}! 👋", fontWeight = FontWeight.Bold, fontSize = 23.sp)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            placeholder = { Text("Buscar jogos...") }, leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(13.dp))
        Spacer(Modifier.height(16.dp))
        when (val result = state) {
            LoadState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is LoadState.Error -> ErrorContent(result.message, vm::refresh)
            is LoadState.Success -> {
                val filtered = result.data.filter {
                    it.title.contains(query, true) || it.genre.orEmpty().contains(query, true)
                }
                if (filtered.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nenhum jogo encontrado.") }
                else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                    if (query.isBlank()) {
                        item { SectionTitle("Destaques") }
                        item { LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(filtered.take(5)) { game -> FeaturedCard(game) { openDetails(game.id) } }
                        } }
                        item { SectionTitle("Novidades") }
                    } else item { SectionTitle("Resultados (${filtered.size})") }
                    items(filtered, key = { it.id }) { game -> GameRow(game.title, game.thumbnail.orEmpty(), game.genre.orEmpty()) { openDetails(game.id) } }
                    item { Text("Dados: FreeToGame.com", color = Color.Gray, fontSize = 12.sp) }
                }
            }
        }
    }
}

@Composable private fun SectionTitle(text: String) { Text(text, fontWeight = FontWeight.Bold, fontSize = 19.sp, modifier = Modifier.padding(vertical = 8.dp)) }

@Composable private fun FeaturedCard(game: GameDto, click: () -> Unit) {
    Card(modifier = Modifier.width(250.dp).clickable(onClick = click), colors = CardDefaults.cardColors(containerColor = Surface)) {
        AsyncImage(model = game.thumbnail, contentDescription = game.title,
            modifier = Modifier.fillMaxWidth().height(140.dp), contentScale = ContentScale.Crop)
        Text(game.title, modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold)
        Text(game.genre.orEmpty(), modifier = Modifier.padding(start = 10.dp, bottom = 10.dp), color = Accent, fontSize = 12.sp)
    }
}

@Composable private fun GameRow(title: String, imageUrl: String, genre: String, onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Surface)) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = imageUrl, contentDescription = title, contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 94.dp, height = 68.dp).clip(RoundedCornerShape(9.dp)))
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Text(genre, color = Accent, fontSize = 12.sp)
            }
            if (trailing != null) trailing()
            else Icon(Icons.Default.ChevronRight, null, tint = Accent)
        }
    }
}

@Composable fun FavoritesScreen(vm: AppViewModel, openDetails: (Int) -> Unit) {
    val favorites by vm.favorites.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        SectionTitle("Favoritos")
        if (favorites.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Você ainda não salvou jogos.\nExplore o catálogo e adicione seus favoritos!", textAlign = TextAlign.Center, color = Color.LightGray)
        } else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(favorites, key = { it.gameId }) { item: FavoriteEntity ->
                GameRow(item.title, item.thumbnail, item.genre, { openDetails(item.gameId) }) {
                    Icon(Icons.Default.Favorite, "Favorito", tint = Color(0xFFFF6182))
                }
            }
        }
    }
}

@Composable fun DetailsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val detail by vm.detail.collectAsState()
    val favorites by vm.favorites.collectAsState()
    when (val result = detail) {
        LoadState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is LoadState.Error -> ErrorContent(result.message, onBack)
        is LoadState.Success -> DetailContent(result.data, favorites.any { it.gameId == result.data.id }, vm::toggleFavorite, onBack)
    }
}

@Composable private fun DetailContent(game: GameDetailDto, favorite: Boolean,
    toggle: (GameDetailDto) -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box {
            AsyncImage(model = game.thumbnail, contentDescription = game.title,
                modifier = Modifier.fillMaxWidth().height(250.dp), contentScale = ContentScale.Crop)
            IconButton(onClick = onBack, modifier = Modifier.padding(12.dp).clip(RoundedCornerShape(12.dp)).background(Background)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
            }
        }
        Column(Modifier.padding(18.dp)) {
            Text(game.title, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Text("${game.genre.orEmpty()}  •  ${game.platform.orEmpty()}", color = Accent)
            Spacer(Modifier.height(24.dp))
            SectionTitle("Sobre o jogo")
            Text(game.description.orEmpty().ifBlank { "Descrição indisponível." }, color = Color.LightGray, lineHeight = 22.sp)
            Spacer(Modifier.height(20.dp))
            Text("Desenvolvedor: ${game.developer.orEmpty()}", color = Color.LightGray)
            Text("Lançamento: ${game.releaseDate.orEmpty()}", color = Color.LightGray)
            Spacer(Modifier.height(26.dp))
            PrimaryButton(if (favorite) "Remover dos favoritos ♥" else "♡ Adicionar aos favoritos", { toggle(game) })
            Spacer(Modifier.height(12.dp))
            Text("Informações: FreeToGame.com", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable fun ProfileScreen(vm: AppViewModel) {
    val user by vm.user.collectAsState()
    val favorites by vm.favorites.collectAsState()
    var newName by remember(user?.id) { mutableStateOf(user?.name.orEmpty()) }
    var editing by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        SectionTitle("Perfil")
        Spacer(Modifier.height(25.dp))
        Icon(Icons.Default.SportsEsports, null, tint = Accent, modifier = Modifier.size(92.dp))
        Spacer(Modifier.height(12.dp))
        Text(user?.name.orEmpty(), fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(user?.email.orEmpty(), color = Color.Gray)
        Spacer(Modifier.height(22.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Surface)) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Favorite, null, tint = Color(0xFFFF6182))
                Text("${favorites.size} Favoritos")
            }
        }
        Spacer(Modifier.height(24.dp))
        if (editing) {
            AppField(newName, { newName = it }, "Seu nome")
            Spacer(Modifier.height(10.dp))
            PrimaryButton("Salvar nome", { vm.saveName(newName); editing = false })
        } else OutlinedButton(onClick = { newName = user?.name.orEmpty(); editing = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Edit, null); Spacer(Modifier.width(8.dp)); Text("Editar conta")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = { vm.logout() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null); Spacer(Modifier.width(8.dp)); Text("Sair")
        }
    }
}

@Composable private fun ErrorContent(message: String, retry: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.WifiOff, null, tint = Accent, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(18.dp))
        Text(message, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        PrimaryButton("Tentar novamente", retry)
    }
}
