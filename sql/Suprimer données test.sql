-- Vide les trois tables, gère les clés étrangères (CASCADE)
-- et réinitialise les compteurs SERIAL (RESTART IDENTITY).
TRUNCATE 
    public.Module, 
    public.Sound, 
    public.Interaction
RESTART IDENTITY 
CASCADE;