package com.poc.foodreceipe.presentation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.poc.foodreceipe.domain.model.ModelResult
import com.poc.foodreceipe.presentation.navigation.BottomNavigation
import com.poc.foodreceipe.presentation.navigation.Routes
import com.poc.foodreceipe.utils.ShimmerRecipeCardItem
import com.poc.foodreceipe.presentation.viewModels.MainViewModel
import com.poc.foodreceipe.presentation.viewModels.events.MainEvent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    // Collect the UI state from the ViewModel
    val uiState by mainViewModel.uiState.collectAsState()
    val netState by mainViewModel.netState.collectAsState()
    val bookedRecipes by mainViewModel.uiState.collectAsState()

    // State hoisting for selected recipe and bottom sheet
    var selectedRecipe by remember { mutableStateOf<ModelResult?>(null) }
    val modalSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }

    // Filter recipes based on the UI state
    val filteredRecipes = remember(uiState.recipes) {
        uiState.recipes.filter {
            it.title.isNotEmpty()
        }
    }

    LaunchedEffect(netState.isNetworkAvailable, netState.networkMessage) {
        if (!netState.isNetworkAvailable && uiState.recipes.isEmpty()) {
            snackBarHostState.showSnackbar(
                message = "Connection Lost.",
                duration = SnackbarDuration.Short
            )
        }

        netState.networkMessage?.let { message ->
            snackBarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
                withDismissAction = true
            )
            mainViewModel.onEvent(MainEvent.ClearError)
            mainViewModel.onEvent(MainEvent.RefreshRecipes)
        }
    }

    if (showBottomSheet && selectedRecipe != null) {
        ModalBottomSheet(
            dragHandle = null,
            onDismissRequest = {
                showBottomSheet = false
                selectedRecipe = null
            },
            sheetState = modalSheetState
        ) {
            RecipePreviewContent(
                recipe = selectedRecipe!!,
                onViewFullRecipe = {
                    showBottomSheet = false
                    navController.navigate(Routes.RecipeScreen.route + "/${selectedRecipe!!.recipeId}")
                }
            )
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.exclude(WindowInsets.navigationBars),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Recipes",
                        fontWeight = FontWeight.Bold
                    )
                },
               /* actions = {
                    IconButton(onClick = { *//* Handle notification *//* }) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                    RectangularSwitch(
                        checked = isDarkTheme,
                        onCheckedChange = { onToggleTheme() },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }*/
            )
        },
        bottomBar = { BottomNavigation(navController) },
        snackbarHost = {
            Box(modifier = Modifier.fillMaxWidth()) {
                SnackbarHost(
                    hostState = snackBarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(10.dp)
                ) { data ->
                    Snackbar(
                        modifier = Modifier
                            .height(30.dp)
                            .fillMaxWidth(),
                        containerColor = when {
                            !netState.isNetworkAvailable -> Color.Red
                            netState.networkMessage == "Back Online" -> Color.Green
                            else -> MaterialTheme.colorScheme.inverseSurface
                        },
                        contentColor = Color.White
                    ) {
                        Text(
                            text = data.visuals.message,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(
                    top = padding.calculateTopPadding(),
                    start = padding.calculateStartPadding(LayoutDirection.Ltr),
                    end = padding.calculateEndPadding(LayoutDirection.Ltr)
                )
                .padding(12.dp)
        ) {
            Text(
                "Discover Delicious Recipes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Text(
                "Get ready to embark on a delightful journey, \n all handpicked recipes for you",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            SearchBar(
                value = uiState.searchQuery,
                onValueChange = { query ->
                    mainViewModel.onEvent(MainEvent.SearchRecipes(query))
                },
                placeholder = "Search recipes..."
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            when {
                uiState.isLoading -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(5) {
                            ShimmerRecipeCardItem()
                        }
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                filteredRecipes.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recipes found",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = filteredRecipes,
                            key = { it.recipeId }) { recipe ->

                            RecipeCard(
                                recipe = recipe.recipeId.let { recipe },
                               // isFavorite = bookedRecipes.any { it.id == recipe.recipeId },
                                isFavorite = false,
                                onFavoriteClick = {
                                    mainViewModel.onEvent(MainEvent.AddToFavorites(recipe))
                                },
                                onItemClick = {
                                    selectedRecipe = recipe
                                    showBottomSheet = true
                                },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCard(
    modifier: Modifier = Modifier,
    recipe: ModelResult,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(0.dp))
            .height(240.dp),
        onClick = onItemClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Recipe Image
            AsyncImage(
                model = recipe.image,
                contentDescription = "Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
                onLoading = {},
                onSuccess = {},
                onError = {}
            )

            // Gradient overlay (fades from bottom to top)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.4f)
                            ),
                            startY = -100f
                        )
                    )
            )

            // Favorite button
            IconButton(
                onClick = {
                    onFavoriteClick()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Add to favorites",
                    tint = if (isFavorite) Color.Red else Color.White
                )
            }

            // Bottom Content (Title and Info Row)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                // Recipe Info Row
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "${recipe.readyInMinutes} mins",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Source Name with translucent background
                    recipe.sourceName?.let {
                        Text(
                            text = "By $it",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}


@Composable
fun RectangularSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val thumbColor = if (checked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val trackWidth = 40.dp
    val trackHeight = 20.dp
    val thumbSize = 16.dp

    Box(
        modifier = modifier
            .size(trackWidth, trackHeight)
            .clip(RoundedCornerShape(0.dp)) // Rectangular shape
            .background(trackColor)
            .clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .size(thumbSize)
                .clip(RoundedCornerShape(0.dp)) // Rectangular thumb
                .background(thumbColor)
                .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
        )
    }
}