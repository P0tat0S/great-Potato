/**************************
   Author: Mark Fernandez
   Date: 01/10/19
**************************/

//Import utilities
import java.util.*;
import java.io.*;

class trappedCastle {
   public static void main(String[] args) //MAIN method
   {
      //Initialisation of Player
      Player p;
      Room[] rooms;
      while(true) {
         String game = stringInput("Type 'N' to create a (N)ew Game"
         +"\nType 'L' to (L)oad the previous Game");
         if(game.equalsIgnoreCase("N")) {
            //Player creation
            p = playerCreation();
            playerPrinter(p);
            print("This is your Status Card, to view it, you can type S anywhere to check it."
            +"\n\nThese are your items, you can equip items before leaving a room by typing E");
            p = equipItems(p);

            //Welcome message
            print(randomEntrance() + "\n\nWalking down the entrance of the "
            +"castle you find yourself in the Great Hall. Looking around "
            +"you don't find anything of value or interest with the "
            +"exception of three weird looking exits.");

            //Initialise rooms
            rooms = new Room[300]; //Max of 100 rooms traversed.
            rooms[0] = new Room();
            rooms[0].roomCounter = 1;
            break;
         } else if(game.equalsIgnoreCase("L")) {
            p = loadPlayer();
            rooms = loadRooms();
            break;
         } else {
            print("That was not one of the choices. Try again...");
         }
      }
      mainPremise(p, rooms);
   } //End main

   public static void mainPremise(Player p, Room[] rooms) {
      while (getHealth(p) > 0) {//While loop that will check for health
         //Room generator, rC stands for roomCounter
         int rC = rooms[0].roomCounter;
         print("Room Counter: " + rC);

         //Enter a shop every 30 rooms
         if((rC-1) % 30 == 0 && !(rC==1)) {
            p = enterShop(p);
         }

         if(rC >= 297) {
            p = enterFinalBoss(p);
            if (getHealth(p) <= 0) {
               print("You lost against the final boss better luck next time.");
            } else {
               print("\n\nCongratulations you finished the game"
               + "\n\nFinal Score:");
            }
            playerPrinter(p);
            print("\nGAME OVER");
            System.exit(0);
         } else {
            rooms = generateRoom(rC, rooms);
         }

         //Enter one of the three Rooms
         p = roomEvent(p, rooms, rC);

         //Generate next three rooms
         rC += 3;
         rooms[0].roomCounter = rC;
         //Save Game
         saveGame(p, rooms);

         //Check if dead and exit if true
         if (getHealth(p) <= 0) {
            print("\nYour health has reached " + getHealth(p) + "\n\n" +
               randomDeath() + "\n\nFinal Score:");
            playerPrinter(p);
            print("\nGAME OVER");
            System.exit(0);
         }

         //Check if level Up
         if (getXp(p) >= getXpMax(p)) {
            levelUp(p);
         }
      } //End main while loop
   }

   /********************************************
   Methods to send the player into random events
   ********************************************/
   public static Player roomEvent(Player p, Room[] rooms, int rC) {
      //Before entering check if the player is above max health
      if (getHealth(p) > getMaxHealth(p)) p.health = getMaxHealth(p);
      entranceInfo(rooms, rC); //Room information printer
      String choice = playerChoice(p, rooms, rC);
      if (choice.equalsIgnoreCase("L")) p = enterRoom(p, rooms[rC]);
      else if (choice.equalsIgnoreCase("F")) p = enterRoom(p, rooms[rC + 1]);
      else if (choice.equalsIgnoreCase("R")) p = enterRoom(p, rooms[rC + 2]);
      return p;
   } //End roomEvent

   public static Player enterRoom(Player p, Room room) {
      //Method that sends player into different room types
      print("\nYou have entered " + getRoomName(room));
      if (getRoomType(room).equals("Boss"))
         enterBoss(p);
      else if (getRoomType(room).equals("Combat"))
         enterCombat(p);
      else if (getRoomType(room).equals("Relic"))
         enterRelic(p);
      else if (getRoomType(room).equals("Health"))
         enterHealth(p);
      return p;
   }//End enterRoom

   //Enter one of the different types of rooms
   public static Player enterBoss(Player p) {
      //Method for an monster encounte
      print("\nYou encounter a really though enemy. This wont be easy.\n");
      int difficulty = diceRoller(4, 6); //Random enemy stats
      p = combatPhase(p, difficulty); //Send player to a Combat
      if(getRunAway(p) || getHealth(p) <= 0) {  //Return if you ran away
         p.runAway = false;
         return p;
      }
      p = enemyDrops(p, difficulty);
      p.relics++; //Rewards
      print("The enemy dropped a relic!");
      roomSeparator(); //Prints a line of "-" to cover a room event
      return p; //Returns player after changes
   }//End enterBoss

   public static Player enterCombat(Player p) {
      //Same method as enterBoss with less rewards and less difficult
      print("\nYou encounter an enemy. You will not continue unscathed.\n");
      int difficulty = diceRoller(1, 3);
      p = combatPhase(p, difficulty);
      if(getRunAway(p) || getHealth(p) <= 0) {
         p.runAway = false;
         return p;
      }
      //Method for enemy loot
      p = enemyDrops(p, difficulty);
      roomSeparator();
      return p;
   }//End enterCombat

   public static Player enterRelic(Player p) {
      print("\nIn the middle of the room you see old relics.");
      String trap = randomTrap(); //Random trap descriptions
      while (true) {
         String decision = stringInput(
            "Do you want to pick them up? (Y)es/(N)o");
         if (decision.equalsIgnoreCase("Y")) {
            p.relics += 2;
            int d2 = diceRoller(1, 2);
            if (d2 == 2) {
               print("You manage to grab the relics. But");
               print(trap);
               int damage = diceRoller(1, 3) * getLevel(p);
               p.health -= damage;
               print("You receive " + damage + " points of damage.");
            } else {
               print("You grab the relics successfully and find a few coins" +
                  " lying around.");
               p.money += diceRoller(1, 2) * getLevel(p);
            }
            break;
         } else if (decision.equalsIgnoreCase("N"))   {
            print("You refuse to grab the relics and continue you journey");
            break;
         } else if (decision.equalsIgnoreCase("S")) playerPrinter(p);
         else print("That was not a decision. Try again...");
      } //End while loop
      roomSeparator();
      return p; //Method to get relics(score)
   }

   public static Player enterHealth(Player p) {
      print("\nA room with a fountain in the middle, you rest.");
      int heal = (int)(diceRoller(1, 4) * (getLevel(p) /
      2.0)); //Heal randomly
      p.health += heal;
      print("You have healed for " + heal);
      roomSeparator();
      return p; //Method that heals the player
   }

   public static Player enterShop(Player p) {
      //Initialise price variables
      boolean exit = false;
      print("\nA mysterious looking man in front, looks at you." +
      "\nWelcome traveler, would I interest you on any of my wares:");
      while(!exit) {//While loop that will exit with input E
         //Calculate prices based on curretn max health or mana
         int priceHealth = (int)(100*(getMaxHealth(p)/25.0));
         int priceMana = (int)(100*(getMaxMana(p)/25.0));
         //Print the shop prices
         printShop(priceHealth, priceMana);
         //Get player input and set the max health or mana
         String choice = stringInput("Type the matching number to buy an item"
         + "\nor type 'E' to (E)xit");
         if(choice.equals("1")&&getMoney(p)>=priceHealth) {
            p.money -= priceHealth;
            p.addedHealth += 4;
            p.maxHealth += 4;
            print("Your new maximum health is " + getMaxHealth(p));
            print("Thank you for your patronage");
         } else if(choice.equals("2")&&getMoney(p)>=priceMana) {
            p.money -= priceMana;
            p.addedMana += 4;
            p.maxMana += 4;
            print("Your new maximum mana is " + getMaxMana(p));
            print("Thank you for your patronage");
         } else if (choice.equalsIgnoreCase("E")) {//Exit with input e
            print("Farewell traveler");
            exit = true;
         } else if (choice.equalsIgnoreCase("S")) {
            playerPrinter(p);
         } else {
            print("That item does not exist or you don't have enough money!\n");
         }
      }
      return p;
   }//End enterShop

   public static Player enterFinalBoss(Player p) {
      p = combatPhase(p, 12); //Send player to a Combat
      if(getRunAway(p) || getHealth(p) <= 0) {  //Return if you ran away
         p.runAway = false;
         return p;
      }
      return p;
   }//End enterFinalBoss

   /*************
   Chance Methods
   *************/
   public static Player enemyDrops(Player p, int difficulty) {
      int d2 = diceRoller(1,2);
      int d100 = diceRoller(1,100);
      //Roll for one of the types either weapon or armor
      if (d2==1)  {
         double dropChance =
         (difficulty/(double)diceRoller(1,2))*(getLuck(p)/2.0);
         print("Drop Chance:"+d100+"/"+dropChance);
         if(dropChance>=d100)
            p.weapons = generateWeapon(p, difficulty);
         else print("The enemy did not drop a piece of equipment.");
      } else if (d2==2) {
         double dropChance =
         (difficulty/(double)diceRoller(1,2))*(getLuck(p)/2.0);
         print("Drop Chance:"+d100+"/"+dropChance);
         if(dropChance>=d100)
            p.armors = generateArmor(p, difficulty);
         else print("The enemy did not drop a piece of equipment.");
      }
      return p;//Return palyer with items or not
   }//End enemyDrops

   /*************
   Combat Methods
   *************/
   public static Player combatPhase(Player p, int difficulty) {
      int dangerLevel = difficulty * getLevel(p);
      Enemy e = generateEnemy(dangerLevel); //Enemy Creation
      int enemyStats = getEnemyAttack(e)+getEnemyDefense(e)+getEnemyHealth(e);
      //Temporary buffs
      boolean defended = false;
      boolean buffed = false;
      while (true) //Will break when either is defeated or when running
      {
         combatInfo(p, e); //At start of combat print the state of the combat
         if (getEnemyHealth(e) <= 0) {//If enemy is defeated
            int gainedMoney = (int)(diceRoller(0,5) * getLuck(p) / 20.0  +
            diceRoller(1, 3) * enemyStats / 10.0);
            print("\nThe combat has ended.");
            print("You have gained " + enemyStats + " Experience Points!");
            p.xp += enemyStats;
            print("The enemy dropped " + gainedMoney + " coins!");
            p.money += gainedMoney;
            break; //Stop and get rewards
         }
         //If player is dead
         else if (getHealth(p) <= 0) {
            print("You have lost the combat");
            break; //Stop and GAME OVER
         } else /**COMBAT PROCEDURES**/ {
            /************
            Player's Turn
            ************/
            if (defended) //Return defense to normal at start
            {
               p.defense /= 2;
               defended = false;
            }
            String action = playerTurn(p); //Get player input
            if (action.equalsIgnoreCase("A")) //Attack the enemy
            {
               int damageDealt = damageCalculationP(p, e);
               e.health -= damageDealt;
            } else if (action.equalsIgnoreCase("D")) //Defend the enemy attack
            {
               print("You entered a defensive position");
               p.defense *= 2;
               defended = true;
            } else if (action.equalsIgnoreCase("M")) //Cast a spell
            {
               p = castSpell(p, e);
               e.health -= getMagicDamage(p);
            } else if (action.equalsIgnoreCase("R")) //Run aways
            {
               int runDamage = (int)(diceRoller(1, 3) * enemyStats / 10.0);
               p.health -= runDamage;
               p.runAway = true;
               print("\nYou successfully run away but receive " + runDamage +
                  " points of damage running away.");
               break; //Stop and return damaged
            } //End Player's Turn

            /***********
            Enemy's Turn
            ***********/
            int d10 = diceRoller(1, 10);
            //Enemy actions is either attack or enrage to deal double damage
            if (d10 <= 2 && !buffed && !(getEnemyHealth(e) <=0)) //If enraged
            {
               print("The enemy enrages");
               buffed = true;
            } else if (!(getEnemyHealth(e) <=0)) //Prevent dead enemy action
            {
               if (buffed) //If enraged
               {
                  int enemyDamage = damageCalculationE(p, e, buffed);
                  p.health -= enemyDamage;
                  buffed = false;
               } else {
                  int enemyDamage = damageCalculationE(p, e, buffed);
                  p.health -= enemyDamage;
               }
            } //End Enemy's Turn
         } //End Combat Mechanics
      }
      return p;
   } //End Combat Phase

   public static int damageCalculationP(Player p, Enemy e) {
      //Damage calculation of the player when attacking
      int d100 = diceRoller(1, 100);
      int critical = (int)(0.80 * getLuck(p) + 0.20 * getDexterity(
      p)); //Crit chance
      print("Critical roll:" + d100 + " /" + critical);
      boolean crit = false;
      if (d100 <= critical) //Check if it is a critical attack
      {
         p.attack *= 2; //Double attack
         crit = true;
      }
      int damage = (getAttack(p) - getEnemyDefense(e));//Simple damage formula
      if (damage <= 1) damage = diceRoller(1, getLevel(p));//Random damage
      if (crit) {
         print("CRITICAL HIT!");
         p.attack /= 2;
         crit = false; //Return attack to normal
      }
      print("You attack for " + damage);
      return damage; //Return the damage that the player will deal
   }

   public static int damageCalculationE(Player p, Enemy e, boolean buffed) {
      if (buffed) e.attack *= 2; //Double attack if enraged
      int damage = (getEnemyAttack(e) - getDefense(p));
      if (damage <= 1) damage = diceRoller(1, getLevel(p));
      int d100 = diceRoller(1, 100);
      int evade = (int)(0.80 * getDexterity(p) + 0.20 * getLuck(
      p)); //Evade chance
      print("Evade roll:" + d100 + " /" + evade);
      if (d100 <= evade) //Check if you evaded the attack
      {
         damage = 0;
         print("You evade the attack!");
      }
      if (buffed) e.attack /= 2;
      print("The " + getEnemyType(e) + " attacks and deals " + damage);
      return damage; //Return the damage dealt by the enemy
   }

   public static Player castSpell(Player p, Enemy e) {
      Magic[] m = generateSpells();//Creation of a set of spells
      String choice = "";
      while (true) {
         choice = stringInput(printSpells(p, m)
         +"\nTo cast a Spell, type the corresponding spell number: ");
         if (choice.equals("1")) {//Send Player, Enemy and the Spell into calc.
            p.magicDamage = magicDamage(p, e, m[0]);//Store magic damage dealt
            p.mana -= getManaCost(m[0]);//Substract the mana used
            break;
         //Check if Player has enough mana and level to cast the Spell
         } else if (choice.equals("2") && getLevel(p) >= getUnlockLevel(m[1])
         && getMana(p) >= getManaCost(m[1])) {
            p.magicDamage = magicDamage(p, e, m[1]);
            p.mana -= getManaCost(m[1]);
            break;
         } else if (choice.equals("3") && getLevel(p) >= getUnlockLevel(m[2])
         && getMana(p) >= getManaCost(m[2])) {
            p.magicDamage = magicDamage(p, e, m[2]);
            p.mana -= getManaCost(m[2]);
            break;
         } else if (choice.equals("4") && getLevel(p) >= getUnlockLevel(m[3])
         && getMana(p) >= getManaCost(m[3])) {
            p.magicDamage = magicDamage(p, e, m[3]);
            p.mana -= getManaCost(m[3]);
            break;
         } else if (choice.equals("5") && getLevel(p) >= getUnlockLevel(m[4])
         && getMana(p) >= getManaCost(m[4])) {
            p.magicDamage = magicDamage(p, e, m[4]);
            p.mana -= getManaCost(m[4]);
            break;
         } else if (choice.equalsIgnoreCase("S")) playerPrinter(p);
         else print("You cannot cast that. Please try again...");
      }
      return p;//Return the magic damage dealth within the Player
   }//End castSpell

   public static int magicDamage(Player p, Enemy e, Magic m) {
      int totalDamage = (int)(getDmgMultiplier(m) * getAttack(p) - 0.5 *
         getEnemyDefense(e));//Formula for magic damage
      if (totalDamage<=1)  totalDamage = diceRoller(1, getLevel(p));
      print("You cast " + getSpellName(m) + "!" + "\nYou deal " +
         totalDamage + " damage!");
      return totalDamage;//Return the damaage dealt
   }//End magicDamage


   /****************************************
   Methods that get a choice from the player
   ****************************************/
   public static String playerChoice(Player p, Room[] rooms, int rC) {
      String choice;
      while (true) //Prevent non-wanted letters
      {
         choice = stringInput(
            "\nType a letter to go: (L)eft, (F)orward, (R)ight");
         if (choice.equalsIgnoreCase("L")) break;
         else if (choice.equalsIgnoreCase("F")) break;
         else if (choice.equalsIgnoreCase("R")) break;
         else if (choice.equalsIgnoreCase("S")) playerPrinter(p);
         else if (choice.equalsIgnoreCase("E")) {
            p = equipItems(p);
            entranceInfo(rooms, rC); //Room information printer
         } else print("\nThat was not one of the paths. Please try again.");
      }
      return choice; //Returns the player's choice
   }

   public static String playerTurn(Player p) {
      String choice;
      while (true) //Prevent other letters
      {
         choice = stringInput(
            "\nType a letter to perform an action: (A)ttack," +
            " (M)agic, (D)efend, (R)un");
         if (choice.equalsIgnoreCase("A")) break;
         else if (choice.equalsIgnoreCase("D")) break;
         else if (choice.equalsIgnoreCase("R")) break;
         else if (choice.equalsIgnoreCase("M") && getMana(p) >= 4) break;
         else if (choice.equalsIgnoreCase("M")) print("Not enough mana.");
         else if (choice.equalsIgnoreCase("S")) playerPrinter(p);
         else print("\nThat was not an action. Please try again.");
      }
      return choice; //Returns a letter
   }

   /***************************
   Methods to generate an Enemy
   ***************************/
   public static Enemy generateEnemy(int dangerLevel) {
      Enemy e = new Enemy();
      e.level = dangerLevel; //Set enemy level to its danger level
      double factor = getEnemyLevel(e) / 10.0;
      e.name = randomEnemyName(); //~Sets the name randomly
      e.type = randomEnemyType(); //~Sets the type randomly
      //Each type of enemy has its random range of stats
      if (getEnemyType(e).equals("Zombie")) {
         e.health = (int)((1 + factor) * diceRoller(8, 13));
         e.attack = (int)((1 + factor) * diceRoller(4, 6));
         e.defense = (int)((1 + factor) * diceRoller(5, 8));
      } else if (getEnemyType(e).equals("Goblin")) {
         e.health = (int)((1 + factor) * diceRoller(5, 8));
         e.attack = (int)((1 + factor) * diceRoller(9, 15));
         e.defense = (int)((1 + factor) * diceRoller(2, 4));
      } else if (getEnemyType(e).equals("Slime")) {
         e.health = (int)((1 + factor) * diceRoller(6, 10));
         e.attack = (int)((1 + factor) * diceRoller(2, 4));
         e.defense = (int)((1 + factor) * diceRoller(8, 13));
      } else if (getEnemyType(e).equals("Skeleton")) {
         e.health = (int)((1 + factor) * diceRoller(5, 9));
         e.attack = (int)((1 + factor) * diceRoller(5, 9));
         e.defense = (int)((1 + factor) * diceRoller(5, 9));
      } else if (getEnemyType(e).equals("Dark Knight")) {
         e.health = (int)((1 + factor) * diceRoller(8, 13));
         e.attack = (int)((1 + factor) * diceRoller(9, 15));
         e.defense = (int)((1 + factor) * diceRoller(8, 13));
      }
      return e; //Returns created enemy
   }

   /***********************
   Methods to create a room
   ***********************/
   public static Room[] generateRoom(int rC, Room[] rooms) {
      int roomsToCreate = rC + 3;
      while (rC < roomsToCreate) //while loop that will create the rooms
      {
         rooms[rC] = new Room();
         rooms[rC].roomNumber = rC;
         rooms[rC].roomType = randomRoomType(); //~gets room type
         rooms[rC].roomName = randomRoomName(); //~gets room name
         if (getRoomType(rooms[rC]).equals("Boss")) {
            rooms[rC].dangerLevel = 6; //~sets danger level
         } else if (getRoomType(rooms[rC]).equals("Combat")) {
            rooms[rC].dangerLevel = diceRoller(4, 5);
         } else if (getRoomType(rooms[rC]).equals("Relic")) {
            rooms[rC].dangerLevel = diceRoller(2, 3);
         } else if (getRoomType(rooms[rC]).equals("Health")) {
            rooms[rC].dangerLevel = 1;
         }
         rC++; //Increase the counter and create a new room
      } //End while loop to create rooms
      return rooms;
   } //End generateRoom

   public static String randomRoomType() {
      int d6 = diceRoller(1, 6);
      String type;
      if (d6 == 6) type = "Boss";
      else if (d6 == 4 || d6 == 5 || d6 == 3) type = "Combat";
      else if (d6 == 2) type = "Relic";
      else type = "Health";
      return type; //Method that returns a random type that will define the room
   } //End randomRoomType

   public static String randomRoomName() {
      String roomName = ("The " + randomAdjective() + " " + randomNoun() +
         " room.");
      return roomName; //Method that calls random generators and sets the name
   } //End randomRoomName;

   /********************************************
   Methods to initialise the Player and level Up
   ********************************************/
   public static Player playerCreation() {
      Player p = new Player();
      p.name = stringInput(
      "WELCOME\nWhat is your name?"); //get player's name
      p.job = jobSelection(); //~makes them choose a job
      p = setStats(p); //Set combat stats
      p.xp = 0;
      p.xpMax = 100;
      p.level = 1;
      p.money = 0;
      p.relics = 0;
      Weapon[] weapons =  new Weapon[100];//Max number of Weapons
      p.weapons =  initialiseWeapons(weapons);
      p.weaponEquiped = false;
      Armor[] armors = new Armor[100];
      p.armors = initialiseArmors(armors);
      p.armorEquiped = false;
      return p; //Set everything else to a default state
   } //End playerCreation

   public static String jobSelection() {
      //Method that gives the description of each job and makes them choose one
      String job = "none";
      print("\nChoose a starting job:");
      print("\nWARRIOR: It has the highest HEALTH stat and great ATTACK and " +
         "DEFENSE stats but lacks MANA.");
      print("\nMAGE: It has the highest MANA stat followed by a high ATTACK stat" +
         " stats but lacks HEALTH.");
      print("\nROGUE: It has the highest ATTACK stat followed by a high DEXTERITY" +
         " stat but lacks HEALTH and MANA.");
      print("\nJESTER: Jack of all trades but master of none, perfectly balanced" +
         " stats throughout.");
      print("\nVILLAGER: Weakest character with really low stats" +
         " but with really tremendous LUCK.");
      while (true) //while loop to prevent "other" jobs
      {
         job = stringInput("\nType the job you want to get");
         if (job.equalsIgnoreCase("WARRIOR")) break;
         else if (job.equalsIgnoreCase("MAGE")) break;
         else if (job.equalsIgnoreCase("ROGUE")) break;
         else if (job.equalsIgnoreCase("JESTER")) break;
         else if (job.equalsIgnoreCase("VILLAGER")) break;
         else print("\nThat is not a job, please try again.");
      }
      return job;
   } //End jobSelection

   public static Player setStats(Player p) {
      //Method that will set the player attributes
      double factor = getLevel(p) / 10.0;
      if (getJob(p).equalsIgnoreCase("WARRIOR")) {
         p.health = (int)((1 + factor) * 20);
         p.maxHealth = (int)((1 + factor) * 20);
         p.mana = (int)((1 + factor) * 8);
         p.maxMana = (int)((1 + factor) * 8);
         p.attack = (int)((1 + factor) * 16);
         p.defense = (int)((1 + factor) * 16);
         p.dexterity = (int)((1 + factor) * 12);
         p.luck = (int)((1 + factor) * 12);
      } else if (getJob(p).equalsIgnoreCase("MAGE")) {
         p.health = (int)((1 + factor) * 10);
         p.maxHealth = (int)((1 + factor) * 10);
         p.mana = (int)((1 + factor) * 20);
         p.maxMana = (int)((1 + factor) * 20);
         p.attack = (int)((1 + factor) * 18);
         p.defense = (int)((1 + factor) * 12);
         p.dexterity = (int)((1 + factor) * 10);
         p.luck = (int)((1 + factor) * 14);
      } else if (getJob(p).equalsIgnoreCase("ROGUE")) {
         p.health = (int)((1 + factor) * 12);
         p.maxHealth = (int)((1 + factor) * 12);
         p.mana = (int)((1 + factor) * 8);
         p.maxMana = (int)((1 + factor) * 8);
         p.attack = (int)((1 + factor) * 20);
         p.defense = (int)((1 + factor) * 12);
         p.dexterity = (int)((1 + factor) * 18);
         p.luck = (int)((1 + factor) * 14);
      } else if (getJob(p).equalsIgnoreCase("JESTER")) {
         p.health = (int)((1 + factor) * 14);
         p.maxHealth = (int)((1 + factor) * 14);
         p.mana = (int)((1 + factor) * 14);
         p.maxMana = (int)((1 + factor) * 14);
         p.attack = (int)((1 + factor) * 14);
         p.defense = (int)((1 + factor) * 14);
         p.dexterity = (int)((1 + factor) * 14);
         p.luck = (int)((1 + factor) * 14);
      } else if (getJob(p).equalsIgnoreCase("VILLAGER")) {
         p.health = (int)((1 + factor) * 8);
         p.maxHealth = (int)((1 + factor) * 8);
         p.mana = (int)((1 + factor) * 8);
         p.maxMana = (int)((1 + factor) * 8);
         p.attack = (int)((1 + factor) * 8);
         p.defense = (int)((1 + factor) * 8);
         p.dexterity = (int)((1 + factor) * 8);
         p.luck = (int)((1 + factor) * 44);
      }
      return p;
   } //End setStats

   public static Player levelUp(Player p) {
      print("Congratulations you just leveled up!!!");
      p.level++;
      //Upgrade player's stats, and re-equip stuff.
      p = setStats(p);
      p.attack += p.weaponDamage;
      p.defense += p.armorDefense;
      p.maxHealth += p.addedHealth;
      p.health = getMaxHealth(p);
      p.maxMana += p.addedMana;
      p.mana = getMaxMana(p);
      //Any extra xp is kept for next level
      int extraXP = getXp(p) % getXpMax(p);
      p.xpMax *= 1.2;
      p.xp = extraXP;
      playerPrinter(p);
      return p;
   }

   public static Weapon[] initialiseWeapons(Weapon[] weapons)  {
      //Method to create the basic weapon
      weapons[0] = new Weapon();
      weapons[0].weaponNumber = 0;
      weapons[0].name = "Basic wooden Stick";
      weapons[0].rarity = "Basic";
      weapons[0].level = 1;
      weapons[0].attack = 1;
      weapons[0].numberOfWeapons = 0;
      return weapons;
   }

   public static Armor[] initialiseArmors(Armor[] armors)  {
      //Method to create the basic armor
      armors[0] = new Armor();
      armors[0].armorNumber = 0;
      armors[0].name = "Basic leather Armor";
      armors[0].rarity = "Basic";
      armors[0].level = 1;
      armors[0].defense = 1;
      armors[0].numberOfArmors = 0;
      return armors;
   }

   /*********************
   Methods to equip Items
   *********************/
   public static Player equipItems(Player p) {
      //Method that will send the player to different methods to equip its items
      weaponList(p);//~List print the obtained equipment
      equipWeapon(p);//equip~ changes the value of the player stats
      armorList(p);
      equipArmor(p);
      return p;
   }

   public static Player equipWeapon(Player p) {
      while (true) {
         //Initialise variables, put stored weapons and counter into variables
         int choice = -1;
         Weapon[] weapons = p.weapons;
         int wC = weapons[0].numberOfWeapons;
         boolean notValid = true;
         while(notValid)//Code that will execute until choosen a valid option
      	{
      		try {//Execute the code and look for exceptions
      			String input = stringInput("\nTo equip a weapon type the"
      			+" corresponding number or type O to (O)rder the weapons");
               //If that will check for the letter 'O' to order the record
               if(input.equalsIgnoreCase("O")&& wC> 1)  {
                  p = weaponSort(p, wC);//Send player to sort array of Weapon
                  weaponList(p);
                  print("Weapon List Sorted");
               }
      			choice = Integer.parseInt(input);
               notValid = false;
      		} catch (NumberFormatException e)   {
               print("That is not a number. Try again...");
            }
      	}
         if(choice <= wC && choice >- 1) {
            //If there is a weapons equipped unequip current weapon
            if(getWeaponEquiped(p)) p.attack -= p.weaponDamage;
            //Equip the chosen weapon and save the value in p.weaponDamage
            p.attack += weapons[choice].attack;
            p.weaponDamage = weapons[choice].attack;
            p.weaponEquiped = true;//Set the equip status to true
            print("\nYour new attack value is "+getAttack(p)+"\n");
            break;//Only stop if is valid
         } else {
            print("There is no such item. Try again...");
         }
      }
      return p;
   }

   public static Player equipArmor(Player p) {
      //Same method as equipWeapon
      while (true) {
         int choice = -1;
         Armor[] armors = p.armors;
         int aC = armors[0].numberOfArmors;
         boolean notValid = true;
         while(notValid)
      	{
      		//Execute the code and look for exceptions
      		try {
      			String input = stringInput("\nTo equip an armor type the"
      			+" corresponding number or type O to (O)rder the armors");
               if(input.equalsIgnoreCase("O")&& aC> 1)  {
                  p = armorSort(p, aC);
                  armorList(p);
                  print("Armor List Sorted");
               }
      			choice = Integer.parseInt(input);
      			notValid = false;
      		} catch (NumberFormatException e)   {
      			print("That is not a number. Try again...");
      		}
      	}
         if(choice <= aC && choice >- 1) {
            if(getArmorEquiped(p)) p.defense -= p.armorDefense;
            p.defense += armors[choice].defense;
            p.armorDefense = armors[choice].defense;
            p.armorEquiped = true;
            print("\nYour new defense value is "+getDefense(p)+"\n");
            break;
         } else {
            print("There is no such item. Try again...");
         }
      }
      return p;
   }

   /***********************
   Method to create spells
   ***********************/
   public static Magic[] generateSpells() {
      //Method that will set each variable of the Record Magic in a for loop
      Magic[] m = new Magic[5];
      String[] spellNames = {
         "Magic Missile", "Frost Bolt", "Fireball",
         "Thundercrack", "Void Blast" };
      int[] unlockLevels = { 1, 3, 6, 9, 12 };
      int[] manaCosts = { 4, 8, 12, 16, 20 };
      double[] dmgMultipliers = { 0.75, 1.00, 1.25, 1.50, 2.00 };
      //For loop that uses the previous to set the spells
      for (int i = 0; i < m.length; i++) {
         m[i] = new Magic();
         m[i].spellName = spellNames[i];
         m[i].unlockLevel = unlockLevels[i];
         m[i].manaCost = manaCosts[i];
         m[i].dmgMultiplier = dmgMultipliers[i];
      }
      return m;
   }

   /***********************************
   Methods to create Weapons and Armors
   ***********************************/
   public static Weapon[] generateWeapon(Player p, int difficulty)   {
      //Counter inside the class
      Weapon[] weapons = p.weapons;//Put weapons inside the player into a var.
      weapons[0].numberOfWeapons++;//Add the number of weaoins created
      int wC = weapons[0].numberOfWeapons;//wC is weapon Counter
      //Set Weapon stats
      weapons[wC] = new Weapon();
      weapons[wC].weaponNumber = wC;
      //Method that returns a rarity based on enemy difficulty and player luck
      weapons[wC].rarity = rarityChance(getLuck(p), difficulty);
      weapons[wC].level = getLevel(p);
      //Method that will set the name of the weapon
      weapons[wC].name = (getWeaponRarity(weapons[wC]) + " " + randomAdjective()
      + " " + randomWeapon() +" +"+ getWeaponLevel(weapons[wC]));
      //Method that will return a value based on its rarity
      weapons[wC].attack = weaponDamage(getWeaponRarity(weapons[wC]), getWeaponLevel(weapons[wC]));
      print("The enemy has dropped a weapon!!!");
      weaponPrinter(weapons[wC]);
      return weapons;
   }

   public static Armor[] generateArmor(Player p, int difficulty)   {
      //Same method as generateWeapon but with armor
      Armor[] armors = p.armors;
      armors[0].numberOfArmors++;
      int aC = armors[0].numberOfArmors;//aC is armor Counter
      //Set Armor stats
      armors[aC] = new Armor();
      armors[aC].armorNumber = aC;
      armors[aC].rarity = rarityChance(getLuck(p), difficulty);
      armors[aC].level = getLevel(p);
      armors[aC].name = (getArmorRarity(armors[aC]) + " " + randomAdjective()
      + " " + randomArmor() +" armor +"+ getArmorLevel(armors[aC]));
      armors[aC].defense = armorDefense(getArmorRarity(armors[aC]), getArmorLevel(armors[aC]));
      print("The enemy has dropped an armor!!!");
      armorPrinter(armors[aC]);
      return armors;
   }

   public static String rarityChance(int luck, int quality) {
      int d100 = diceRoller(1,100);
      //Set the rarity of a weapon given a chance
      String rarity = "";
      if (d100 <= 3 + (int)(quality/3.0) + (int)(luck/12.0))
         rarity = "Legendary";
      else if (d100 <= 9 + (int)(quality/2.0) + (int)(luck/6.0))
         rarity = "Epic";
      else if (d100 <= 22 + quality + (int)(luck/3.0))
         rarity = "Rare";
      else if (d100 <= 48)
         rarity = "Uncommon";
      else if (d100 <= 100)
         rarity = "Common";
      return rarity;//Return the set strinf rarity
   }

   public static int weaponDamage(String rarity, int level)   {
      int damage = 0;//Initialise damage
      //Set the damage equal to a formula depending on rarity
      if (rarity.equals("Legendary")) {
         damage = 5*(diceRoller(1,10)) + 32 + 3*level;
      } else if (rarity.equals("Epic")) {
         damage = 4*(diceRoller(1,10)) + 16 + 2*level;
      } else if (rarity.equals("Rare")) {
         damage = 3*(diceRoller(1,10)) + 8 + 2*level;
      } else if (rarity.equals("Uncommon")) {
         damage = 2*(diceRoller(1,10)) + 4 + 1*level;
      } else if (rarity.equals("Common")) {
         damage = 1*(diceRoller(1,10)) + 2 + 1*level;
      }
      return damage;//Return the randomized damage
   }

   public static int armorDefense(String rarity, int level)   {
      int armor = 0;//Initialise armor
      if (rarity.equals("Legendary")) {
         armor = 5*(diceRoller(1,10)) + 32 + 3*level;
      } else if (rarity.equals("Epic")) {
         armor = 4*(diceRoller(1,10)) + 16 + 2*level;
      } else if (rarity.equals("Rare")) {
         armor = 3*(diceRoller(1,10)) + 8 + 2*level;
      } else if (rarity.equals("Uncommon")) {
         armor = 2*(diceRoller(1,10)) + 4 + 1*level;
      } else if (rarity.equals("Common")) {
         armor = 1*(diceRoller(1,10)) + 2 + 1*level;
      }
      return armor;//Same method as weaponDamage
   }


   /**************
   Utility methods
   **************/
   public static void print(String message) {
      System.out.println(message);
      return; //Method that abbreviates the println method
   } //End print

   public static String stringInput(String message) {
      print(message);
      Scanner sc = new Scanner(System.in); //Creation of a scanner
      String input = sc.nextLine();
      return input; //Method that returns the user's string input
   } //End stringInput

   public static int diceRoller(int min, int max) {
      Random random = new Random(); //Creation of an object Random
      //Gets two integers and returns a random value between the given values
      int dice = random.nextInt(max + 1 - min) + min;
      return dice;
   } //End diceRoller

   /**********************
   User Interface printers
   **********************/
   public static void playerPrinter(Player p) {
      print("\n******************************************");
      print("       Name:" + getName(p) + "      Job:" + getJob(p));
      print("       XP:" + getXp(p) + "/" + getXpMax(p) + "      Level:"
           +getLevel(p));
      print("       Money:" + getMoney(p) + "        Relics:" + getRelics(p));
      print("       Health:" + getHealth(p) + "/" + getMaxHealth(p)
           +"   Mana:" + getMana(p) + "/" + getMaxMana(p));
      print("       Attack:" + getAttack(p) + "      Defense:" + getDefense(p));
      print("       Dexterity:" + getDexterity(p) + "   Luck:" + getLuck(p));
      print("******************************************");
      return; //Status card printer of the Player
   } //End playerPrinter

   public static void roomSeparator() {
      print("----------------------------------------------------------------"
       + "-------------");
      return; //Method that just prints -'s
   }

   public static void entranceInfo(Room[] rooms, int rC) {
      String door = randomDoor();
      print("\nBefore you the three " + door + " stand. Small signs above, " +
         "give you the name of the rooms and their danger level:" +
         "\n\n" + "(L)" + getRoomName(rooms[rC]) +
         "\nDanger Level:" + diceRoller(1, 3)*getDangerLevel(rooms[rC]) +
         "\n\n" + "(F)" + getRoomName(rooms[rC + 1]) +
         "\nDanger Level:" + diceRoller(1, 3) * getDangerLevel(rooms[rC + 1]) +
         "\n\n" + "(R)" + getRoomName(rooms[rC + 2]) +
         "\nDanger Level:" + diceRoller(1, 3) * getDangerLevel(rooms[rC +2]));
      return; //Prints the information of all tree rooms in the intersection
   }

   public static void combatInfo(Player p, Enemy e) {
      //Method that will print a .txt file corresponding to an enemy
      try {
         if (getEnemyType(e).equals("Zombie")) {
            File file = new File("./sprites/zombie.txt");//Read file
            //Usage of a buffered reader to read each character until the end
            BufferedReader bR = new BufferedReader(new FileReader(file));
            String line; //Store text into a single String
            while ((line = bR.readLine()) !=null) //Read all the lines
               print(line); //Print the whole file
         } else if (getEnemyType(e).equals("Goblin")) {
            File file = new File("./sprites/goblin.txt");
            BufferedReader bR = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bR.readLine()) != null)
               print(line);
         } else if (getEnemyType(e).equals("Slime")) {
            File file = new File("./sprites/slime.txt");
            BufferedReader bR = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bR.readLine()) != null)
               print(line);
         } else if (getEnemyType(e).equals("Skeleton")) {
            File file = new File("./sprites/skeleton.txt");
            BufferedReader bR = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bR.readLine()) != null)
               print(line);
         } else if (getEnemyType(e).equals("Dark Knight")) {
            File file = new File("./sprites/dark_Knight.txt");
            BufferedReader bR = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bR.readLine()) != null)
               print(line);
         }
      } catch (Exception exception) {
         print("Enemy files not found");
         exception.printStackTrace();//Print the error that no files are found
      }
      print(getEnemyName(e) + " the " + getEnemyType(e));
      print("Health:" + getEnemyHealth(e) + " Attack:" + getEnemyAttack(e) +
         " Defense:" + getEnemyDefense(e));
      print("\n" + getName(p) + " the " + getJob(p));
      print("Health:" + getHealth(p) + " Mana:" + getMana(p) + " Attack:" +
         getAttack(p) + " Defense:" + getDefense(p));
      return;
   }

   public static String printSpells(Player p, Magic[] m) {
      print("\nAll spells ignore half of the enemy's defenses.");
      String spells = "";
      int spellsPrinted = 0;
      for (int i = 0; i < 5; i++) { //Concatenate spell information.
         spells += "\n\nSpell " + "(" + (i + 1) + ")" + ": " +
            getSpellName(m[i]) + "\nMana Cost: " + getManaCost(m[i]) +
            "\nDamage multiplier: " + getDmgMultiplier(m[i]);
         spellsPrinted++; //Counter to print next spells after checking level
         if (getLevel(p) < getUnlockLevel(m[1]))
            break;//Stop if less than the next unlock level '3'
         else if (getLevel(p) < getUnlockLevel(m[2]) && spellsPrinted == 2)
            break;//Stop after printing two spells and cheking the level
         else if (getLevel(p) < getUnlockLevel(m[3]) && spellsPrinted == 3)
            break;
         else if (getLevel(p) < getUnlockLevel(m[4]) && spellsPrinted == 4)
            break;//Same stoppers until max lvl
      }
      return spells;
   }

   public static void weaponPrinter(Weapon w)   {
      //Prints the name and the attack value of the weapon
      print(getWeaponName(w));
      print("Damage: "+ getWeaponAttack(w));
      return;
   }

   public static void armorPrinter(Armor a)   {
      //Same as weaponPrinter but with defense
      print(getArmorName(a));
      print("Armor: "+ getArmorDefense(a));
      return;
   }

   public static void weaponList(Player p)   {
      print("==============================================================");
      //Put the array of weapons inside player into a variable
      Weapon[] weapons = p.weapons;
      int wC = weapons[0].numberOfWeapons;//wC is weapon Counter
      for(int i = 0; i <= wC ; i++)  {//Print only the max number of weapons
         print("\nWeapon "+ "(" + i + ")");
         weaponPrinter(weapons[i]);//Call a method to finely print the weapons
      }
      print("\n==============================================================");
      return;
   }

   public static void armorList(Player p)   {
      //Same as the weaponLIst method
      print("==============================================================");
      Armor[] armors = p.armors;
      int aC = armors[0].numberOfArmors;//aC is armor Counter
      for(int i = 0; i <= aC ; i++)  {
         print("\nArmor "+ "(" + i + ")");
         armorPrinter(armors[i]);
      }
      print("\n==============================================================");
      return;
   }

   public static void printShop(int priceHealth, int priceMana) {
      print("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
      print("\n(1) Empty Heart Container");
      print("Increases the maximum Health by 4");
      print("Price: " + priceHealth);
      print("\n(2) Empty Magicka Container");
      print("Increases the maximum Mana by 4");
      print("Price: " + priceMana);
      print("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
      return;
   }

   /*********************************
   Methods to get random descriptions
   *********************************/
   public static String randomEntrance() {
      //Random number between 1 and 3
      int d3 = diceRoller(1, 3);
      String message;
      if (d3 == 1) {
         message =
            "A regular gate with wide metal doors, a regular bridge and" +
            " a moat guards the only place with water within these hot," +
            " dry lands and it's the only way in.";
      } else if (d3 == 2) {
         message =
            "A great gate with thick wooden doors and hot oil pots guards" +
            " the only passage into the castle build upon a mountain top" +
            " and it's the only easy way in.";
      } else {
         message =
            "A vast gate with enormous metal doors, a regular bridge and" +
            " hot oil pots guards the only entrance to the castle build" +
            " at the edges of a shoreline and it's the only way in.";
      }
      return message; //returns one of the tree entrance messages
   } //End randomEntrance

   public static String randomDeath() {
      //Random number between 1 and 3
      int d3 = diceRoller(1, 3);
      String message;
      if (d3 == 1) {
         message =
            "I can't take this much longer. My entire body screams, " +
            "telling me to lie down and sleep. I must resist. If I sleep" +
            " I'll surely die, but I can't take this much longer. I'll" +
            " just.. I'll just lie down for a little while. Save my" +
            " energy, I'll make it out of this mess soon enough.";
      } else if (d3 == 2) {
         message =
            "Oh god, I can see it now. This is my end. My body is broken" +
            " beyond repair, I grow weaker by the minute and nobody is" +
            " going to find me. It would be too late even if they did." +
            " I'm going to die. That's okay, I give up.";
      } else {
         message =
            "I can't move, I can't think, I can't do anything. This can" +
            " only mean the end for me, I'm beyond the point of no return" +
            ". I'm going to die.. I'm going to die! No, no, no, no, no!" +
            " Please, I don't want to die.";
      }
      return message;//Return a block of text
   } //End randomDeath

   public static String randomAdjective() {
      int d50 = diceRoller(0, 49);
      String[] adjectives = {"scintillating","acceptable","barbarous","flashy",
      "noxious","laughable","normal","hurried","puzzled","oval","overrated",
      "scandalous","dark","bloody","adventurous","lethal","groovy","whole",
      "adaptable","alive","smoltering","different","lopsided","malicious",
      "troubled","knowledgeable","simplistic","brash","encouraging","dusty",
      "inexpensive","smart","incandescent","weary","workable","grandiose",
      "splendid","precious","tragic","narrow","joyous","sharp","careless",
      "flat","resonant","bumpy","solid","strong","faulty","accidental"};
      return adjectives[d50]; //Returns a random adjective inside the array
   } //End randomAdjective

   public static String randomNoun() {
      int d50 = diceRoller(0, 49);
      String[] nouns = {"rise","campaign","secret","past","safety","pipe",
      "credit","rule","shelter","connection","farm","food","wealth","ratio",
      "failure","power","raw","system","square","money","sky","bear","black",
      "disease","pressure","final","weakness","data","beacon","equal",
      "penalty","keep","associate","control","elevator","company","concept",
      "disk","perception","professional","field","bone","natural","peak",
      "deep","future","extreme","night","knife","procedure"};
      return nouns[d50]; //Returns a random noun inside the array
   } //End randomNoun

   public static String randomDoor() {
      int d10 = diceRoller(0, 9);
      String[] doors = {"Gates","Doors","Openings","Portals","Apertures",
      "Entries","Hatches","Fissures","Rifts","Vents"};
      return doors[d10]; //Returns a random synonim of aperture
   } //End randomDoor

   public static String randomTrap() {
      int d4 = diceRoller(0, 3);
      String[] traps = {
         "Negative energy pillars and ethereal scythes hit you.",
         "Icy metal disks and precise ballistae shoot towards you.",
         "Forceful spears and energy-draining javelins get thrown towards you.",
         "Sonic swinging glaives and necrotic floortiles damage you." };
      return traps[d4]; //Same as other random Methods
   }

   public static String randomWeapon() {
      int d10 = diceRoller(0,9);
      String[] nameWeapons = {"Sword", "Bow", "Spear", "Crossbow", "Dagger",
      "Mace","Staff", "Greatsword", "Club", "Battleaxe"};
      return nameWeapons[d10];
   }

   public static String randomArmor()  {
      int d10 = diceRoller(0,9);
      String[] nameArmors = {"Leather", "Plate", "Chainmail", "Iron", "Gold",
      "Invisible","Cardboard", "Wooden", "Damascus Steel", "Diamond"};
      return nameArmors[d10];
   }

   public static String randomEnemyName() {
      int d40 = diceRoller(0, 39);
      String[] enemyName = {"Moransab","Mavorgezu","Thargha","Zergta","Vresan",
      "Thild'ula","Ha","Fenu","Imilphu","Bucu","Ronba","Nexla","Doomimgash",
      "Ball","Xyal","Grorn","Raruk","Mali","Thanxus","Irath","Rend","Hevorg",
      "Kil'grorn","Thusra","Bachom","Rornushang","Rothme","Dresh",
      "Rothshandze","Reshra","Naush","Motar","Baalshu","Lavi","Phekahud",
      "Zargver","Rath","Varorsharg","Kukruul","Becain"};
      return enemyName[d40]; //Returns a random enemy name
   } //End randomEnemyName

   public static String randomEnemyType() {
      int d5 = diceRoller(0, 4);
      String[] enemyType = {"Zombie","Slime","Skeleton","Goblin","Dark Knight"};
      return enemyType[d5]; //Returns a random enemy type
   }

   /******************
   Bubble Sort Methods
   ******************/
   public static Player weaponSort(Player p, int wC)   {
      Weapon[] weapons = p.weapons;
		//Counter for number of comparisons
		int nReal=0;
		int nPhase=0;
		boolean realSorted = false;
		//While loop that repeats each phase
		while (realSorted == false)   {
			nReal=0;
			boolean sorted = false;
			int n =0;
			//While loop that compares and shifts the values
			while (sorted == false)  {
				if(weapons[n].attack <= weapons[n+1].attack) {
					n++;
					nReal++;
				} else {
					//Shift of values
					Weapon temp = weapons[n];
					weapons[n] = weapons[n+1];
					weapons[n+1] = temp;
					n++;
				}
				if(n == wC) {
					//Reset once all values are compared
					n=0;
					sorted = true;
				}
			}
			nPhase++;
			if (nReal == wC)  {
				realSorted=true;
			}
		}
		return p;
   }

   public static Player armorSort(Player p, int aC)   {
      Armor[] armors = p.armors;
		//Counter for number of comparisons
		int nReal=0;
		int nPhase=0;
		boolean realSorted = false;
		//While loop that repeats each phase
		while (realSorted == false)   {
			nReal=0;
			boolean sorted = false;
			int n =0;
			//While loop that compares and shifts the values
			while (sorted == false)  {
				if(armors[n].defense <= armors[n+1].defense) {
					n++;
					nReal++;
				} else {
					//Shift of values
					Armor temp = armors[n];
					armors[n] = armors[n+1];
					armors[n+1] = temp;
					n++;
				}
				if(n == aC) {
					//Reset once all values are compared
					n=0;
					sorted = true;
				}
			}
			nPhase++;
			if (nReal == aC)  {
				realSorted=true;
			}
		}
		return p;
   }

   /***********************
   Input and Output Methods
   ***********************/
   public static void saveGame(Player p, Room[] rooms) {
      try {
         ObjectOutputStream player = new ObjectOutputStream(new FileOutputStream("./saveData/playerData.txt"));//Save to this path
         player.writeObject(p);//Save data to a file
         ObjectOutputStream room = new ObjectOutputStream(new FileOutputStream("./saveData/roomData.txt"));
         room.writeObject(rooms);//Same as with player
         print("Game saved");
      } catch(Exception e) {
         print("NOTHING WAS SAVED");//Print this if the folder does not exist
         e.printStackTrace();//Print the error
      }
   }

   public static Player loadPlayer() {
      Player p = new Player();//Crete a PLayer where you will load the data
      try {
         //Check if there is any file to load
         ObjectInputStream player = new ObjectInputStream(
         new FileInputStream("./saveData/playerData.txt"));//Load from a file
         p = (Player) player.readObject();//Cast into player type and copy data
         return p;//Return the data read as the object player
      } catch(Exception e) {
         print("No game to Load. EXITING...");
         System.exit(0);//Exit if there is no data to load
      }
      return p;
   }

   public static Room[] loadRooms() {
      //Same method as loadPlayer but with the object Room[]
      Room[] rooms = new Room[300];
      try {
         ObjectInputStream room = new ObjectInputStream(
         new FileInputStream("./saveData/roomData.txt"));
         rooms = (Room[]) room.readObject();
         return rooms;
      } catch(Exception e) {
         print("No game to Load. EXITING...");
         System.exit(0);
      }
      return rooms;
   }

   /**************************
   All PLAYER Accessor Methods
   **************************/
   public static String getJob(Player p) { return p.job; }
   public static String getName(Player p) { return p.name; }
   public static int getXp(Player p) { return p.xp; }
   public static int getXpMax(Player p) { return p.xpMax; }
   public static int getLevel(Player p) { return p.level; }
   public static int getMoney(Player p) { return p.money; }
   public static int getRelics(Player p) { return p.relics; }
   public static int getHealth(Player p) { return p.health; }
   public static int getMaxHealth(Player p) { return p.maxHealth; }
   public static int getMana(Player p) { return p.mana; }
   public static int getMaxMana(Player p) { return p.maxMana; }
   public static int getAttack(Player p) { return p.attack; }
   public static int getDefense(Player p) { return p.defense; }
   public static int getDexterity(Player p) { return p.dexterity; }
   public static int getLuck(Player p) { return p.luck; }
   public static int getMagicDamage(Player p) { return p.magicDamage; }
   public static Weapon[] getWeapons(Player p) { return p.weapons; }
   public static boolean getRunAway(Player p) { return p.runAway; }
   public static boolean getWeaponEquiped(Player p) { return p.weaponEquiped; }
   public static boolean getArmorEquiped(Player p) { return p.armorEquiped; }

   /***********************
   All ROOM Accesor Methods
   ***********************/
   public static int getRoomNumber(Room r) { return r.roomNumber; }
   public static String getRoomName(Room r) { return r.roomName; }
   public static String getRoomType(Room r) { return r.roomType; }
   public static int getDangerLevel(Room r) { return r.dangerLevel; }

   /************************************
   All ENEMY Accessor and Setter Methods
   ************************************/
   public static String getEnemyName(Enemy e) { return e.name; }
   public static String getEnemyType(Enemy e) { return e.type; }
   public static int getEnemyHealth(Enemy e) { return e.health; }
   public static int getEnemyLevel(Enemy e) { return e.level; }
   public static int getEnemyAttack(Enemy e) { return e.attack; }
   public static int getEnemyDefense(Enemy e) { return e.defense; }
   public static Enemy setEnemyName(Enemy e, String name) {
      e.name = name;
      return e;
   }
   public static Enemy setEnemyType(Enemy e, String type) {
      e.type = type;
      return e;
   }
   public static Enemy setEnemyHealth(Enemy e, int health) {
      e.health = health;
      return e;
   }
   public static Enemy setEnemyLevel(Enemy e, int level) {
      e.level = level;
      return e;
   }
   public static Enemy setEnemyAttack(Enemy e, int attack) {
      e.attack = attack;
      return e;
   }
   public static Enemy setEnemyDefense(Enemy e, int defense) {
      e.defense = defense;
      return e;
   }

   /*************************
   All MAGIC Accessor Methods
   *************************/
   public static String getSpellName(Magic m) { return m.spellName; }
   public static int getUnlockLevel(Magic m) { return m.unlockLevel; }
   public static int getManaCost(Magic m) { return m.manaCost; }
   public static double getDmgMultiplier(Magic m) { return m.dmgMultiplier; }

   /**************************
   All WEAPON Accessor Methods
   **************************/
   public static String getWeaponName(Weapon w) { return w.name; }
   public static String getWeaponRarity(Weapon w) { return w.rarity; }
   public static int getWeaponLevel(Weapon w) { return w.level; }
   public static int getWeaponAttack(Weapon w) { return w.attack; }

   /**************************
   All ARMOR Accessor Methods
   **************************/
   public static String getArmorName(Armor a) { return a.name; }
   public static String getArmorRarity(Armor a) { return a.rarity; }
   public static int getArmorLevel(Armor a) { return a.level; }
   public static int getArmorDefense(Armor a) { return a.defense; }

} //End class trappedCastle

/******
Records
******/
//Implements Serializable is to save the object to a .txt file
class Player implements Serializable {
   String name;
   String job;
   int level;
   int xp;
   int xpMax;
   int health;
   int maxHealth;
   int mana;
   int maxMana;
   int attack;
   int defense;
   int luck;
   int dexterity;
   int relics;
   int money;
   int magicDamage;
   Weapon[] weapons;
   Armor[] armors;
   boolean runAway;
   int weaponDamage;
   boolean weaponEquiped;
   int armorDefense;
   boolean armorEquiped;
   int addedHealth;
   int addedMana;
} //End class Player

class Room implements Serializable {
   int roomCounter;
   int roomNumber;
   String roomName;
   String roomType;
   int dangerLevel;
} //End class Room

class Enemy {
   String name;
   String type;
   int health;
   int level;
   int attack;
   int defense;
} //End class Enemy

class Weapon implements Serializable {
   int weaponNumber;
   String name;
   String rarity;
   int level;
   int attack;
   int numberOfWeapons;
} //End class Weapon

class Armor implements Serializable {
   int armorNumber;
   String name;
   String rarity;
   int level;
   int defense;
   int numberOfArmors;
} //End class Armor

class Magic {
   String spellName;
   int unlockLevel;
   int manaCost;
   double dmgMultiplier;
}//End class Magic
