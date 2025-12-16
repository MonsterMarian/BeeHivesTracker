# Poznamky

## Načítání dat v jiném Thread
### v ProgramLoop
na začátku metody run je

#### LoginManager loginManager = new LoginManager();
#### this.currentUser = loginManager.showLoginMenu();

při volání konstruktoru se ve třídě LoginManeger spustí další vlákno které přečte udaje ze souboru s daty.


this.dataLoadingFuture = CompletableFuture.runAsync(() -> {//run Async spustí program mimo hlavní vlákno
dataManager.reloadDataFromFile();
});

#### v .showLoginMenu()
je tam část kodu:

try {
dataLoadingFuture.get(5, TimeUnit.SECONDS);
} catch (InterruptedException | ExecutionException | TimeoutException e) {
System.out.println("Warning: Data loading timed out. Using previously loaded data.");
}

čeká max 5s na načtení všech dat


## ActivityLogger neboli zámek pro soubor

### používá ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
#### lock může najednou číst více uživatelů zapisovat může jen jeden, pokud někdo zapisuje lock se zavře a nikdo ho nemůže číst pokudn někdo čte tak se zavře jen pro toho kdo chce zapisovat

## DataManager to stejné co v ActivityLogger

