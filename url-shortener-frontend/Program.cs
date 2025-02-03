using System.Text;
using System.Text.Json;

namespace url_shortener_frontend;

class Program
{
    private static readonly HttpClient Client = new HttpClient();
    private const string BaseUrl = "http://localhost:8080/api";
    private static string _userId = "";

    public static async Task Main()
    {
        Console.WriteLine("Welcome to the URL Shortener Client!");

        while (true)
        {
            Console.WriteLine("\n1. Log In");
            Console.WriteLine("2. Create New User");
            Console.WriteLine("3. Exit");
            Console.Write("Choose an option: ");

            string? choice = Console.ReadLine();

            switch (choice)
            {
                case "1":
                    await LoginUser();
                    break;
                case "2":
                    await CreateUser();
                    break;
                case "3":
                    return;
                default:
                    Console.WriteLine("Invalid choice. Please try again.");
                    continue;
            }
            break; // Exit this loop after successful login or user creation
        }

        while (true)
        {
            Console.WriteLine("\nOptions:");
            Console.WriteLine("1. Shorten a URL");
            Console.WriteLine("2. Retrieve your URLs");
            Console.WriteLine("3. Resolve a short URL");
            Console.WriteLine("4. View all URLs");
            Console.WriteLine("5. Exit");
            Console.Write("Choose an option: ");

            string? choice = Console.ReadLine();
            switch (choice)
            {
                case "1":
                    await ShortenUrl();
                    break;
                case "2":
                    await RetrieveUserUrls();
                    break;
                case "3":
                    await ResolveShortUrl();
                    break;
                case "4":
                    await RetrieveAllUrls();
                    break;
                case "5":
                    return;
                default:
                    Console.WriteLine("Invalid option. Please try again.");
                    break;
            }
        }
    }

    private static async Task LoginUser()
    {
        Console.Write("Enter your email to log in: ");
        string? email = Console.ReadLine();

        var requestBody = new { email };
        string json = JsonSerializer.Serialize(requestBody);
        var content = new StringContent(json, Encoding.UTF8, "application/json");

        HttpResponseMessage response = await Client.PostAsync($"{BaseUrl}/users/login", content);
        if (response.IsSuccessStatusCode)
        {
            string responseData = await response.Content.ReadAsStringAsync();
            try
            {
                var user = JsonSerializer.Deserialize<UserResponse>(responseData);
                if (user != null)
                {
                    _userId = user.userId;
                    Console.WriteLine($"Logged in as {user.email} (User ID: {_userId})");
                }
                else 
                {
                    Console.WriteLine("Error: User object is null.");
                }
            }
            catch (JsonException)
            {
                Console.WriteLine("Error: Unexpected response format when logging in. Response: " + responseData);
            }
        }
        else
        {
            Console.WriteLine("Login failed. User not found or incorrect email.");
        }
    }

    private static async Task CreateUser()
    {
        Console.Write("Enter your email to create a new user: ");
        string? email = Console.ReadLine();

        var requestBody = new { email };
        string json = JsonSerializer.Serialize(requestBody);
        var content = new StringContent(json, Encoding.UTF8, "application/json");

        HttpResponseMessage response = await Client.PostAsync($"{BaseUrl}/users", content);
        if (response.IsSuccessStatusCode)
        {
            string responseData = await response.Content.ReadAsStringAsync();
            try
            {
                var user = JsonSerializer.Deserialize<UserResponse>(responseData);
                if (user != null)
                {
                    _userId = user.userId;
                    Console.WriteLine($"User created: {user.email} (User ID: {_userId})");
                }
                else
                {
                    Console.WriteLine("Error: User object is null.");
                }
            }
            catch (JsonException)
            {
                Console.WriteLine("Error: Unexpected response format when creating user. Response: " + responseData);
            }
        }
        else
        {
            string errorData = await response.Content.ReadAsStringAsync();
            Console.WriteLine("Error creating user. Response: " + errorData);
        }
    }
    

    private static async Task RetrieveUserUrls()
    {
        HttpResponseMessage response = await Client.GetAsync($"{BaseUrl}/users/{_userId}/urls");
        if (response.IsSuccessStatusCode)
        {
            string responseData = await response.Content.ReadAsStringAsync();
            var urls = JsonSerializer.Deserialize<List<UrlResponse>>(responseData);

            Console.WriteLine("Your URLs:");
            foreach (var url in urls)
            {
                Console.WriteLine($"Short: swisscom.com/{url.shortCode} -> {url.longUrl}");
            }
        }
        else
        {
            Console.WriteLine("Error retrieving URLs.");
        }
    }

    private static async Task ShortenUrl()
    {
        Console.Write("Enter the URL to shorten (must start with http:// or https://): ");
        string? longUrl = Console.ReadLine();

        if (!longUrl.StartsWith("http://") && !longUrl.StartsWith("https://"))
        {
            Console.WriteLine("Invalid URL format. Please enter a valid HTTP or HTTPS URL.");
            return;
        }

        var requestBody = new { longUrl, userId = _userId };
        string json = JsonSerializer.Serialize(requestBody);
        var content = new StringContent(json, Encoding.UTF8, "application/json");

        Console.WriteLine("Sending request to shorten URL...");
        Console.WriteLine($"Request Body: {json}");

        HttpResponseMessage response = await Client.PostAsync($"{BaseUrl}/urls/shorten", content);
        string responseData = await response.Content.ReadAsStringAsync();
        
        if (response.IsSuccessStatusCode)
        {
            Console.WriteLine($"Shortened URL: {responseData}");
        }
        else
        {
            Console.WriteLine("Error shortening URL.");
            Console.WriteLine($"Response Code: {response.StatusCode}");
            Console.WriteLine($"Response Body: {responseData}");
        }
    }

    private static async Task ResolveShortUrl()
    {
        Console.Write("Enter the full shortened URL (e.g., swisscom.com/Ltvc8Kp): ");
        string fullShortUrl = Console.ReadLine();

        if (!fullShortUrl.Contains("/"))
        {
            Console.WriteLine("Invalid format. Please enter the full shortened URL.");
            return;
        }

        string shortCode = fullShortUrl.Split("/")[^1]; // Extract short code from URL

        Console.WriteLine($"Extracted short code: {shortCode}");
        Console.WriteLine($"Sending request to resolve: {BaseUrl}/urls/{shortCode}");

        HttpResponseMessage response = await Client.GetAsync($"{BaseUrl}/urls/{shortCode}");
        string responseData = await response.Content.ReadAsStringAsync();

        Console.WriteLine($"Response Code: {response.StatusCode}");

        if (response.StatusCode == System.Net.HttpStatusCode.Found)
        {
            if (response.Headers.Location != null)
            {
                string longUrl = response.Headers.Location.ToString();
                Console.WriteLine($"Redirects to: {longUrl}");
            }
            else
            {
                Console.WriteLine("Short URL resolved, but no Location header found.");
            }
        }
        else if (response.IsSuccessStatusCode)  // Handles 200 OK until I fix the 302 response in the api
        {
            if (!string.IsNullOrWhiteSpace(responseData))
            {
                Console.WriteLine($"*Pretend your browser got redirected to the full URL*");
            }
            else
            {
                Console.WriteLine("Short URL resolved, but response body is empty.");
            }
        }
        else
        {
            Console.WriteLine($"Error: {response.StatusCode}");
        }
    }


    private static async Task RetrieveAllUrls()
    {
        HttpResponseMessage response = await Client.GetAsync($"{BaseUrl}/urls");
        if (response.IsSuccessStatusCode)
        {
            string responseData = await response.Content.ReadAsStringAsync();
            var urls = JsonSerializer.Deserialize<List<UrlResponse>>(responseData);

            Console.WriteLine("All URLs:");
            foreach (var url in urls)
            {
                Console.WriteLine($"Short: swisscom.com/{url.shortCode} -> {url.longUrl}");
            }
        }
        else
        {
            Console.WriteLine("Error retrieving all URLs.");
        }
    }
}

public class UserResponse
{
    public string userId { get; set; }
    public string email { get; set; }
}

public class UrlResponse
{
    public string id { get; set; }
    public string longUrl { get; set; }
    public string shortCode { get; set; }
}